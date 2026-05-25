<?php

namespace App\Http\Middleware;

use Closure;
use Illuminate\Http\Request;
use Symfony\Component\HttpFoundation\Response;

class RequireAdmin
{
    public function handle(Request $request, Closure $next): Response
    {
        $role = $request->header('X-User-Role') ?: '';
        
        if (strtolower($role) !== 'admin') {
            return response()->json(['error' => 'Admin authorization is required'], 403);
        }

        // Attach user metadata to request attributes if passed
        $userId = $request->header('X-User-Id');
        $email = $request->header('X-User-Email');
        if ($userId) {
            $request->attributes->set('user', [
                'id' => $userId,
                'role' => $role,
                'email' => $email
            ]);
        }

        return $next($request);
    }
}
