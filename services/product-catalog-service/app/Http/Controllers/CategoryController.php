<?php

namespace App\Http\Controllers;

use App\Models\Category;
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
}
