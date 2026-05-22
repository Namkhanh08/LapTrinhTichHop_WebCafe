<?php

namespace App\Http\Middleware;

use Closure;
use Illuminate\Http\Request;
use Symfony\Component\HttpFoundation\Response;

class RequireAdmin
{
    public function handle(Request $request, Closure $next): Response
    {
        $payload = $this->readJwtPayload($request);
        if (!$payload || ($payload['role'] ?? null) !== 'admin') {
            return response()->json(['error' => 'Admin authorization is required'], 403);
        }

        // Attach user info to request if needed
        $request->attributes->set('user', $payload);

        return $next($request);
    }

    private function readJwtPayload(Request $request): ?array
    {
        $auth = $request->header('Authorization') ?: '';
        if (!str_starts_with(strtolower($auth), 'bearer ')) {
            return null;
        }

        $token = substr($auth, 7);
        $parts = explode('.', $token);
        if (count($parts) !== 3) {
            return null;
        }

        [$header, $payload, $signature] = $parts;
        $secret = env('JWT_SECRET', 'revo-dev-secret-change-before-production');
        
        $expected = $this->base64UrlEncode(hash_hmac('sha256', "{$header}.{$payload}", $secret, true));
        if (!hash_equals($expected, $signature)) {
            return null;
        }

        $decoded = json_decode($this->base64UrlDecode($payload), true);
        if (!is_array($decoded)) {
            return null;
        }

        if (isset($decoded['exp']) && time() > (int)$decoded['exp']) {
            return null;
        }

        return $decoded;
    }

    private function base64UrlEncode(string $value): string
    {
        return rtrim(strtr(base64_encode($value), '+/', '-_'), '=');
    }

    private function base64UrlDecode(string $value): string
    {
        $padded = strtr($value, '-_', '+/');
        $padded .= str_repeat('=', (4 - strlen($padded) % 4) % 4);
        return base64_decode($padded) ?: '';
    }
}
