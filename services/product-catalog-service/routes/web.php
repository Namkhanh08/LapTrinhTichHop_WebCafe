<?php

use Illuminate\Support\Facades\Route;

Route::get('/', function () {
    return response()->json(['message' => 'Revo Coffee Catalog API']);
});

// Added root level health endpoint to match docker-compose.yml healthcheck
Route::get('/health', function () {
    return response()->json(['status' => 'ok', 'service' => 'product-catalog-service-laravel']);
});
