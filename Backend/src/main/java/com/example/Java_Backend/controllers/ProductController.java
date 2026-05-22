package com.example.Java_Backend.controllers;

import com.example.Java_Backend.entities.Product;
import com.example.Java_Backend.entities.ProductDetail;
import com.example.Java_Backend.repositories.ProductRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("api/products")
@CrossOrigin("*")
public class ProductController{
    @Autowired
    private ProductRepository _productRepo;

    @GetMapping
    public List<Product> getAll() {
        System.out.println("Đang nhận yêu cầu lấy danh sách sản phẩm... ");

        List<Product> List = _productRepo.findAllManual();

        System.out.println("Đã lấy được " + List.size() + " sản phẩm từ SQL Server");
        return List;
    }

    @GetMapping("/{id}")
    public ProductDetail getByProductId(@PathVariable int id){

        return _productRepo.findProductById(id);
    }

    @GetMapping("/quiz-match")
    public List<Product> getQuizMatchedProducts(@RequestParam(required = false) String flavorNotes,
                                                @RequestParam(required = false) String region,
                                                @RequestParam(required = false) String process,
                                                @RequestParam(required = false) String roast,
                                                @RequestParam(required = false) String height){
        System.out.println("Đang phân tích Quiz: flavor=" + flavorNotes + ", height=" + height);
        return _productRepo.findMatchedProductsByQuiz(flavorNotes, region, process, roast, height);
    }
    
}