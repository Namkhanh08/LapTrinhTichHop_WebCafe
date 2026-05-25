<?php

namespace App\Models;

use Illuminate\Database\Eloquent\Model;
use Illuminate\Database\Eloquent\Relations\BelongsTo;
use Illuminate\Database\Eloquent\Relations\BelongsToMany;
use Illuminate\Support\Str;

class Product extends Model
{
    protected $table = 'products';
    protected $keyType = 'string';
    public $incrementing = false;

    protected $fillable = [
        'id', 'name', 'description', 'price', 'category_id', 'type', 
        'region', 'altitude', 'processing_method', 
        'roast_level', 'flavor_notes', 'image_url', 'stock', 'is_active'
    ];

    protected $casts = [
        'price' => 'float',
        'stock' => 'integer',
        'is_active' => 'boolean',
    ];

    protected static function boot()
    {
        parent::boot();
        
        static::creating(function ($model) {
            if (empty($model->id)) {
                $model->id = (string) Str::uuid();
            }
        });
    }

    public function category(): BelongsTo
    {
        return $this->belongsTo(Category::class);
    }

    public function flavorNotes(): BelongsToMany
    {
        return $this->belongsToMany(FlavorNote::class, 'product_flavor_notes', 'product_id', 'flavor_note_id');
    }

    public function grindSizes(): BelongsToMany
    {
        return $this->belongsToMany(GrindSize::class, 'product_grind_sizes', 'product_id', 'grind_size_id');
    }
}
