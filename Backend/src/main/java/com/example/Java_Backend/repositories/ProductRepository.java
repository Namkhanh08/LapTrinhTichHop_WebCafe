package com.example.Java_Backend.repositories;

import com.example.Java_Backend.entities.Product;
import com.example.Java_Backend.entities.ProductDetail;
import com.example.Java_Backend.entities.GrindingOption;
import com.example.Java_Backend.entities.ProductGrindingOption;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.ArrayList;
import java.math.BigDecimal;

@Repository
public class ProductRepository {
    @Autowired
    private JdbcTemplate jdbcTemplate;

    public List<Product> findAllManual(){
        String sql = "SELECT * FROM Products";

        return jdbcTemplate.query(sql, new RowMapper<Product>() {
            @Override
            public Product mapRow(ResultSet rs, int rowNum) throws SQLException {
                Product p = new Product();
                p.setId(rs.getInt("Id"));
                p.setName(rs.getString("Name"));
                p.setPrice(rs.getDouble("Price"));
                p.setStock(rs.getInt("Stock"));
                p.setImageUrl(rs.getString("ImageUrl"));
                p.setDescription(rs.getString("Description"));
                p.setCategoryId(rs.getInt("CategoryId"));
                return p;
            }
        });
    }

    public ProductDetail findProductById(int id){
        String sql = "SELECT PD.Id as pdId, PD.ProductId, PD.Region, PD.Process, PD.Roast, PD.FlavorNotes, PD.Weight, PD.Height, \n" +
                "P.Id as pId, P.Name, P.Price, P.Stock, P.ImageUrl, P.Description, P.CategoryId,\n" +
                "G.Id as gId, G.Name as gName\n" +
                "FROM ProductDetails PD \n" +
                "INNER JOIN Products P ON P.Id = PD.ProductId\n" +
                "INNER JOIN ProductGrindingOptions PGO ON PGO.ProductId = P.Id\n" +
                "INNER JOIN GrindingOptions G ON G.Id = PGO.GrindingOptionId\n" +
                "WHERE PD.ProductId = ?";

        List<ProductDetail> rows = jdbcTemplate.query(sql, (rs, rowNum) -> {
            ProductDetail pd = new ProductDetail();

            pd.setId(rs.getInt("pdId"));
            pd.setProductId(rs.getInt("ProductId"));
            pd.setRegion(rs.getString("Region"));
            pd.setProcess(rs.getString("Process"));
            pd.setRoast(rs.getString("Roast"));
            pd.setFlavorNotes(rs.getString("FlavorNotes"));
            pd.setWeight(rs.getString("Weight"));
            pd.setHeight(rs.getString("Height"));

            Product p = new Product();
            p.setId(rs.getInt("pId"));
            p.setName(rs.getString("Name"));
            p.setPrice(rs.getDouble("Price"));
            p.setStock(rs.getInt("Stock"));
            p.setImageUrl(rs.getString("ImageUrl"));
            p.setDescription(rs.getString("Description"));
            p.setCategoryId(rs.getInt("CategoryId"));

            pd.setProduct(p);

            // set grinding option tạm (1 row)
            GrindingOption g = new GrindingOption();
            g.setId(rs.getInt("gId"));
            g.setName(rs.getString("gName"));

            pd.setGrindingOption(List.of(g)); // tạm

            return pd;
        }, id);

        if (rows.isEmpty()) return null;

        // gom lại thành 1 object
        ProductDetail result = rows.get(0);
        List<GrindingOption> options = new ArrayList<>();

        for (ProductDetail row : rows) {
            options.addAll(row.getGrindingOption());
        }

        result.setGrindingOption(options);

        return result;
    }

    public Product getById(int productId){
        String sql = "SELECT * FROM Products WHERE Id = ?";
        List<Product> product = jdbcTemplate.query(sql, (rs, rowNum) -> {
            Product p = new Product();

            p.setId(rs.getInt("Id"));
            p.setName(rs.getString("Name"));
            p.setDescription(rs.getString("Description"));
            p.setPrice(rs.getDouble("Price"));
            p.setStock(rs.getInt("Stock"));
            p.setImageUrl(rs.getString("ImageUrl"));
            p.setCategoryId(rs.getInt("CategoryId"));

            return p;
        }, productId);

        if(product.isEmpty()){
            return null;
        }
        return product.get(0);
    }

    public void Update(Product product) {
        String sql = """
        UPDATE Products
        SET Name = ?, 
            Description = ?, 
            Price = ?, 
            Stock = ?, 
            ImageUrl = ?, 
            CategoryId = ?
        WHERE Id = ?
        """;

        jdbcTemplate.update(
                sql,
                product.getName(),
                product.getDescription(),
                product.getPrice(),
                product.getStock(),
                product.getImageUrl(),
                product.getCategoryId(),
                product.getId()
        );
    }

    public BigDecimal getProductPrice(Long productId) {

        String sql = """
            SELECT Price
            FROM Products
            WHERE Id = ?
        """;

        return jdbcTemplate.queryForObject(
                sql,
                new Object[]{productId},
                BigDecimal.class
        );
    }

    public List<Product> findMatchedProductsByQuiz(String flavorNotes, String region, String process, String roast, String height){
        StringBuilder sql = new StringBuilder("SELECT DISTINCT P.* FROM Products P " +
                "INNER JOIN ProductDetails PD ON P.Id = PD.ProductId WHERE 1=1 ");

        List<Object> params = new ArrayList<>();

        //Lọc theo hương vị
        if(flavorNotes != null && !flavorNotes.isEmpty()){
            sql.append("AND PD.FlavorNotes LIKE ?");
            params.add("%" + flavorNotes + "%");
        }

        //Lọc theo vùng trồng
        if(region != null && !region.isEmpty()){
            sql.append(" AND PD.Region LIKE ? ");
            params.add("%" + region + "%");
        }
        //Lọc theo phương pháp sơ chế
        if(process != null && !process.isEmpty()){
            sql.append(" AND PD.Process LIKE ? ");
            params.add("%" + process + "%");
        }
        //Lọc theo Mức độ rang (Roast)
        if (roast != null && !roast.isEmpty()) {
            sql.append(" AND PD.Roast LIKE ? ");
            params.add("%" + roast + "%");
        }

        //Lọc theo Độ cao (Height / Altitude)
        if (height != null && !height.isEmpty()) {
            sql.append(" AND PD.Height LIKE ? ");
            params.add("%" + height + "%");
        }

        List<Product> matchedList = jdbcTemplate.query(sql.toString(), (rs, rowNum) -> {
            Product p = new Product();
            p.setId(rs.getInt("Id"));
            p.setName(rs.getString("Name"));
            p.setPrice(rs.getDouble("Price"));
            p.setStock(rs.getInt("Stock"));
            p.setImageUrl(rs.getString("ImageUrl"));
            p.setDescription(rs.getString("Description"));
            p.setCategoryId(rs.getInt("CategoryId"));
            return p;
        }, params.toArray());

        // Nếu lọc quá sâu không ra hạt nào, trả về danh sách Manual mặc định để không lỗi giao diện
        if (matchedList.isEmpty()) {
            return findAllManual();
        }

        return matchedList;
    }

}
