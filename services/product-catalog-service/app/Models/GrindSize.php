<?php

namespace App\Models;

use Illuminate\Database\Eloquent\Model;
use Illuminate\Database\Eloquent\Relations\BelongsToMany;

class GrindSize extends Model
{
    protected $table = 'grind_sizes';
    protected $fillable = ['name'];
    
    public $timestamps = false;

    public function products(): BelongsToMany
    {
        return $this->belongsToMany(Product::class, 'product_grind_sizes', 'grind_size_id', 'product_id');
    }
}
