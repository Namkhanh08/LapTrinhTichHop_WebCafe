<?php

use Illuminate\Database\Migrations\Migration;
use Illuminate\Database\Schema\Blueprint;
use Illuminate\Support\Facades\Schema;

return new class extends Migration
{
    public function up(): void
    {
        Schema::create('grind_sizes', function (Blueprint $table) {
            $table->integer('id')->autoIncrement();
            $table->string('name', 50)->unique();
        });
    }

    public function down(): void
    {
        Schema::dropIfExists('grind_sizes');
    }
};
