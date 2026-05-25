<?php

use Illuminate\Database\Migrations\Migration;
use Illuminate\Database\Schema\Blueprint;
use Illuminate\Support\Facades\DB;
use Illuminate\Support\Facades\Schema;

return new class extends Migration
{
    public function up(): void
    {
        Schema::create('products', function (Blueprint $table) {
            $table->string('id', 36)->primary();
            $table->string('name', 255);
            $table->text('description')->nullable();
            $table->decimal('price', 12, 2);
            $table->integer('category_id')->nullable();
            $table->string('type', 50)->nullable();
            $table->string('region', 100)->nullable();
            $table->string('altitude', 50)->nullable();
            $table->string('processing_method', 100)->nullable();
            $table->string('roast_level', 50);
            $table->string('flavor_notes', 255)->nullable();
            $table->string('image_url', 500)->nullable();
            $table->integer('stock')->default(0);
            $table->boolean('is_active')->default(true);
            $table->timestamps(); // created_at, updated_at

            $table->foreign('category_id')->references('id')->on('categories')->onDelete('set null');
            
            // Chỉ mục cho việc lọc sản phẩm nhanh chóng
            $table->index('type');
            $table->index('region');
            $table->index('roast_level');
            $table->index('processing_method');
        });

        DB::statement('ALTER TABLE products ADD CONSTRAINT chk_products_price_non_negative CHECK (price >= 0)');
        DB::statement('ALTER TABLE products ADD CONSTRAINT chk_products_stock_non_negative CHECK (stock >= 0)');
    }

    public function down(): void
    {
        Schema::dropIfExists('products');
    }
};
