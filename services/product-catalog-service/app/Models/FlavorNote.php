<?php

namespace App\Models;

use Illuminate\Database\Eloquent\Model;
use Illuminate\Database\Eloquent\Relations\BelongsToMany;

class FlavorNote extends Model
{
    protected $table = 'flavor_notes';
    protected $fillable = ['name'];
    
    public $timestamps = false;

    public function products(): BelongsToMany
    {
        return $this->belongsToMany(Product::class, 'product_flavor_notes', 'flavor_note_id', 'product_id');
    }
}
