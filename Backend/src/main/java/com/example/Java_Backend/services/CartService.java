package com.example.Java_Backend.services;

import com.example.Java_Backend.entities.Cart;
import com.example.Java_Backend.entities.CartItem;
import com.example.Java_Backend.repositories.CartRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
public class CartService {
    @Autowired
    private CartRepository _cartRepo;

    public Cart getCart(String userId){
        Cart cart = _cartRepo.findByUserId(userId);
        if(cart == null){
            return null;
        }
        List<CartItem> items = _cartRepo.getCartItemsByUser(userId);
        cart.setItems(items);
        return cart;
    }

    public void addToCart(String userId, int productId, int quantity, int grindingOptionId, String flavorNotes, String weight){
        if(quantity <= 0){
            throw new RuntimeException("Quantity must to rather zero!");
        }

        Cart c = _cartRepo.findByUserId(userId);
        if(c == null){
            _cartRepo.createCart(userId);
            c = _cartRepo.findByUserId(userId);
        }

        CartItem ci = _cartRepo.findByCartAndProduct(c.getId(), productId, grindingOptionId, flavorNotes, weight);
        if(ci != null){
            int newQuantity = ci.getQuantity() + quantity;
            _cartRepo.updateQuantity(userId, ci.getProductId(), newQuantity, grindingOptionId, flavorNotes, weight);
        }else{
            _cartRepo.addItem(c.getId(), productId, quantity, grindingOptionId, flavorNotes, weight);
        }
    }

    public void uppdateCartItem(String userId, int productId, int quantity, int grindingOptionId, String flavorNotes, String weight){
        if(quantity <= 0) _cartRepo.deleteItem(productId,grindingOptionId,flavorNotes,weight);
        else _cartRepo.updateQuantity(userId, productId, quantity, grindingOptionId, flavorNotes,weight);
    }

    public void removeItem(int productId, int grindingOptionId, String flavorNotes, String weight){
        _cartRepo.deleteItem(productId, grindingOptionId, flavorNotes, weight);
    }
}
