<?php

use Illuminate\Database\Migrations\Migration;
use Illuminate\Database\Schema\Blueprint;
use Illuminate\Support\Facades\Schema;

return new class extends Migration
{
    public function up(): void
    {
        Schema::create('product_flavor_notes', function (Blueprint $table) {
            $table->string('product_id', 36);
            $table->integer('flavor_note_id');
            $table->primary(['product_id', 'flavor_note_id']);
            $table->index('flavor_note_id');

            $table->foreign('product_id')->references('id')->on('products')->onDelete('cascade');
            $table->foreign('flavor_note_id')->references('id')->on('flavor_notes')->onDelete('cascade');
        });

        Schema::create('product_grind_sizes', function (Blueprint $table) {
            $table->string('product_id', 36);
            $table->integer('grind_size_id');
            $table->primary(['product_id', 'grind_size_id']);
            $table->index('grind_size_id');

            $table->foreign('product_id')->references('id')->on('products')->onDelete('cascade');
            $table->foreign('grind_size_id')->references('id')->on('grind_sizes')->onDelete('cascade');
        });
    }

    public function down(): void
    {
        Schema::dropIfExists('product_grind_sizes');
        Schema::dropIfExists('product_flavor_notes');
    }
};
