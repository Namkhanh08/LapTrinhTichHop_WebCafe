<?php

namespace App\Http\Controllers;

use App\Models\Category;
use Illuminate\Http\Request;
use Illuminate\Http\JsonResponse;

class CategoryController extends Controller
{
    public function index(): JsonResponse
    {
        $items = Category::orderBy('name')->get();
        return response()->json([
            'items' => $items,
            'total' => $items->count()
        ]);
    }

    public function store(Request $request): JsonResponse
    {
        $data = $request->validate([
            'name' => 'required|string|max:100',
            'description' => 'nullable|string',
        ]);

        $category = Category::create($data);
        return response()->json(['message' => 'Category created', 'category' => $category], 201);
    }

    public function update(Request $request, int $id): JsonResponse
    {
        $category = Category::find($id);
        if (!$category) {
            return response()->json(['error' => 'Category not found'], 404);
        }

        $data = $request->validate([
            'name' => 'sometimes|required|string|max:100',
            'description' => 'nullable|string',
        ]);

        $category->fill($data);
        $category->save();

        return response()->json(['message' => 'Category updated', 'category' => $category]);
    }

    public function destroy(int $id): JsonResponse
    {
        $category = Category::find($id);
        if (!$category) {
            return response()->json(['error' => 'Category not found'], 404);
        }
        if ($category->products()->exists()) {
            return response()->json(['error' => 'Category is still used by products'], 409);
        }

        $category->delete();
        return response()->json(['message' => 'Category deleted']);
    }
}
