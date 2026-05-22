<?php

namespace App\Http\Controllers;

use App\Models\Product;
use App\Models\FlavorNote;
use App\Models\GrindSize;
use Illuminate\Http\Request;
use Illuminate\Http\JsonResponse;
use Illuminate\Support\Str;

class ProductController extends Controller
{
    public function index(Request $request): JsonResponse
    {
        $query = Product::where('is_active', true)->with(['category', 'flavorNotes', 'grindSizes']);

        // Search filter
        if ($request->filled('search')) {
            $search = '%' . $request->input('search') . '%';
            $query->where(function ($q) use ($search) {
                $q->where('name', 'like', $search)
                  ->orWhere('description', 'like', $search)
                  ->orWhere('type', 'like', $search)
                  ->orWhere('region', 'like', $search)
                  ->orWhere('roast_level', 'like', $search)
                  ->orWhere('flavor_notes', 'like', $search)
                  ->orWhere('process_method', 'like', $search)
                  ->orWhere('processing_method', 'like', $search)
                  ->orWhereHas('category', function ($catQ) use ($search) {
                      $catQ->where('name', 'like', $search);
                  });
            });
        }

        // Search Price Range (Always applied if present)
        if ($request->filled('searchMinPrice')) {
            $query->where('price', '>=', $request->input('searchMinPrice'));
        }
        if ($request->filled('searchMaxPrice')) {
            $query->where('price', '<=', $request->input('searchMaxPrice'));
        }

        // Filter Group
        $filterGroup = strtolower($request->input('filterGroup', 'none'));
        if ($filterGroup === 'price') {
            if ($request->filled('minPrice')) {
                $query->where('price', '>=', $request->input('minPrice'));
            }
            if ($request->filled('maxPrice')) {
                $query->where('price', '<=', $request->input('maxPrice'));
            }
        } elseif ($filterGroup === 'type') {
            $type = $request->input('type');
            if ($request->filled('type') && $type !== 'all') {
                $query->where('type', $type);
            }
        } elseif ($filterGroup === 'region') {
            if ($request->filled('regions')) {
                $regions = array_values(array_filter(array_map('trim', explode(',', $request->input('regions')))));
                if (count($regions) > 0) {
                    $query->whereIn('region', $regions);
                }
            }
        }

        // Category filters
        $categoryId = $request->input('categoryId', $request->input('category_id'));
        if ($request->filled('categoryId') || $request->filled('category_id')) {
            $query->where('category_id', $categoryId);
        }
        if ($request->filled('category')) {
            $query->whereHas('category', function ($q) use ($request) {
                $q->where('name', $request->input('category'));
            });
        }

        // Check if pagination is requested
        $paginationRequested = $request->has('page') || $request->has('limit');
        $page = (int) $request->input('page', 1);
        $limit = (int) $request->input('limit', 9);
        $limit = max(1, min($limit, 100));

        // Get total count
        $total = $query->count();

        // Sort by created_at DESC
        $query->orderBy('created_at', 'desc');

        if (!$paginationRequested) {
            $products = $query->get();
            $items = $products->map(fn($p) => $this->shapeProduct($p));
            return response()->json([
                'items' => $items,
                'page' => 1,
                'limit' => $total,
                'total' => $total,
                'totalPages' => 1,
            ]);
        }

        $totalPages = (int) ceil($total / $limit);
        $page = max(1, min($page, $totalPages));
        $offset = ($page - 1) * $limit;

        $products = $query->skip($offset)->take($limit)->get();
        $items = $products->map(fn($p) => $this->shapeProduct($p));

        return response()->json([
            'items' => $items,
            'page' => $page,
            'limit' => $limit,
            'total' => $total,
            'totalPages' => $totalPages,
        ]);
    }

    public function show(string $id): JsonResponse
    {
        $product = Product::with(['category', 'flavorNotes', 'grindSizes'])->find($id);
        if (!$product) {
            return response()->json(['error' => 'Product not found'], 404);
        }

        return response()->json($this->shapeProduct($product));
    }

    public function store(Request $request): JsonResponse
    {
        $request->validate([
            'name' => 'required|string|max:255',
            'price' => 'required|numeric|gt:0',
            'roast_level' => 'required|string|in:light,medium,dark,medium-dark,various,mixed',
            'stock' => 'nullable|integer|min:0',
            'category_id' => 'nullable|integer',
        ]);

        $data = $request->all();
        $id = $data['id'] ?? (string) Str::uuid();

        $product = new Product();
        $product->id = $id;
        $product->name = $data['name'];
        $product->description = $data['description'] ?? null;
        $product->price = $data['price'];
        $product->category_id = $data['category_id'] ?? null;
        $product->type = $data['type'] ?? null;
        $product->region = $data['region'] ?? null;
        $product->altitude = $data['altitude'] ?? null;
        
        $processMethod = $data['process_method'] ?? ($data['processing_method'] ?? null);
        $product->process_method = $processMethod;
        $product->processing_method = $processMethod;
        
        $product->roast_level = $data['roast_level'];
        $product->image_url = $data['image_url'] ?? null;
        $product->stock = $data['stock'] ?? 0;
        $product->is_active = $data['is_active'] ?? true;

        // Process flavor notes text
        $flavorNotes = $this->normalizeFlavorNotesList($data);
        $product->flavor_notes = count($flavorNotes) > 0 ? implode(', ', $flavorNotes) : null;

        $product->save();

        // Sync relationships
        $this->syncProductAttributes($product, $data);

        return response()->json([
            'message' => 'Product created',
            'id' => $id,
            'product' => $this->shapeProduct(Product::with(['category', 'flavorNotes', 'grindSizes'])->find($id))
        ], 201);
    }

    public function update(Request $request, string $id): JsonResponse
    {
        $product = Product::find($id);
        if (!$product) {
            return response()->json(['error' => 'Product not found'], 404);
        }

        $request->validate([
            'name' => 'sometimes|required|string|max:255',
            'price' => 'sometimes|required|numeric|gt:0',
            'roast_level' => 'sometimes|required|string|in:light,medium,dark,medium-dark,various,mixed',
            'stock' => 'nullable|integer|min:0',
            'category_id' => 'nullable|integer',
        ]);

        $data = $request->all();

        // Update fields if present
        if ($request->has('name')) $product->name = $data['name'];
        if ($request->has('description')) $product->description = $data['description'];
        if ($request->has('price')) $product->price = $data['price'];
        if ($request->has('category_id')) $product->category_id = $data['category_id'];
        if ($request->has('type')) $product->type = $data['type'];
        if ($request->has('region')) $product->region = $data['region'];
        if ($request->has('altitude')) $product->altitude = $data['altitude'];
        
        if ($request->has('process_method') || $request->has('processing_method')) {
            $processMethod = $data['process_method'] ?? ($data['processing_method'] ?? null);
            $product->process_method = $processMethod;
            $product->processing_method = $processMethod;
        }

        if ($request->has('roast_level')) $product->roast_level = $data['roast_level'];
        if ($request->has('image_url')) $product->image_url = $data['image_url'];
        if ($request->has('stock')) $product->stock = $data['stock'];
        if ($request->has('is_active')) $product->is_active = $data['is_active'];

        // Normalize flavor notes text
        if ($request->has('flavor_notes') || $request->has('flavorNotes')) {
            $flavorNotes = $this->normalizeFlavorNotesList($data);
            $product->flavor_notes = count($flavorNotes) > 0 ? implode(', ', $flavorNotes) : null;
        }

        $product->save();

        // Sync relationships
        $this->syncProductAttributes($product, $data);

        return response()->json([
            'message' => 'Product updated',
            'id' => $id,
            'product' => $this->shapeProduct(Product::with(['category', 'flavorNotes', 'grindSizes'])->find($id))
        ]);
    }

    public function destroy(string $id): JsonResponse
    {
        $product = Product::find($id);
        if (!$product) {
            return response()->json(['error' => 'Product not found'], 404);
        }

        $product->is_active = false;
        $product->save();

        return response()->json([
            'message' => 'Product deactivated',
            'id' => $id
        ]);
    }

    private function shapeProduct(Product $product): array
    {
        // Maintains exact compatibility with shape_product() in old index.php
        $flavorNotesList = $product->flavorNotes->count() > 0
            ? $product->flavorNotes->map(fn($f) => ['id' => $f->id, 'name' => $f->name])->toArray()
            : $this->splitFlavorNotes($product->flavor_notes ?? '');

        return [
            'id' => $product->id,
            'name' => $product->name,
            'description' => $product->description,
            'price' => (float) $product->price,
            'category_id' => $product->category_id !== null ? (int) $product->category_id : null,
            'category_name' => $product->category?->name,
            'type' => $product->type,
            'region' => $product->region,
            'altitude' => $product->altitude,
            'processing_method' => $product->processing_method,
            'roast_level' => $product->roast_level,
            'flavor_notes' => $product->flavor_notes,
            'image_url' => $product->image_url,
            'stock' => (int) $product->stock,
            'is_active' => (bool) $product->is_active,
            'created_at' => $product->created_at?->toIso8601String() ?: now()->toIso8601String(),
            'updated_at' => $product->updated_at?->toIso8601String() ?: now()->toIso8601String(),
            'desc' => $product->description,
            'process' => $product->processing_method,
            'roast' => $product->roast_level,
            'flavorNotes' => $product->flavor_notes,
            'image' => $product->image_url,
            'flavor_notes_list' => $flavorNotesList,
            'grindSizes' => $product->grindSizes->map(fn($g) => ['id' => $g->id, 'name' => $g->name])->toArray(),
        ];
    }

    private function syncProductAttributes(Product $product, array $data): void
    {
        if (array_key_exists('flavor_notes', $data) || array_key_exists('flavorNotes', $data)) {
            $notes = $this->normalizeFlavorNotesList($data);
            $noteIds = [];
            foreach ($notes as $note) {
                $flavorNote = FlavorNote::firstOrCreate(['name' => $note]);
                $noteIds[] = $flavorNote->id;
            }
            $product->flavorNotes()->sync($noteIds);
        }

        $grindIds = $data['grind_size_ids'] ?? ($data['grindSizeIds'] ?? null);
        if (is_array($grindIds)) {
            $ids = array_filter(array_map('intval', $grindIds));
            $product->grindSizes()->sync($ids);
        }
    }

    private function normalizeFlavorNotesList(array $data): array
    {
        $value = $data['flavor_notes'] ?? ($data['flavorNotes'] ?? '');
        if (is_array($value)) {
            return array_values(array_filter(array_map('trim', array_map('strval', $value))));
        }
        return $this->splitFlavorNotes((string) $value);
    }

    private function splitFlavorNotes(string $value): array
    {
        return array_values(array_filter(array_map('trim', explode(',', $value))));
    }
}
