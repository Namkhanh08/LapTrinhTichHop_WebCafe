<?php

use Illuminate\Support\Facades\Route;
use App\Http\Controllers\ProductController;
use App\Http\Controllers\CategoryController;

Route::get('/health', function () {
    return response()->json(['status' => 'ok', 'service' => 'product-catalog-service-laravel']);
});

Route::get('/categories', [CategoryController::class, 'index']);
Route::post('/categories', [CategoryController::class, 'store'])->middleware('admin');
Route::put('/categories/{id}', [CategoryController::class, 'update'])->middleware('admin');
Route::delete('/categories/{id}', [CategoryController::class, 'destroy'])->middleware('admin');

// Read endpoints (Public)
Route::get('/products/filters', [ProductController::class, 'filters']);
Route::get('/products', [ProductController::class, 'index']);
Route::get('/products/{id}', [ProductController::class, 'show']);

// Write endpoints (Admin authorization required)
Route::post('/products', [ProductController::class, 'store'])->middleware('admin');
Route::put('/products/{id}', [ProductController::class, 'update'])->middleware('admin');
Route::patch('/products/{id}', [ProductController::class, 'update'])->middleware('admin');
Route::delete('/products/{id}', [ProductController::class, 'destroy'])->middleware('admin');
