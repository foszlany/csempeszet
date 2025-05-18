package com.hu.mobilalk;

public class ShopItem {
    private String name;
    private String desc;
    private int price;
    private int image_resource;
    private int quantity; // ONLY USED FOR CART

    public ShopItem() {}

    public ShopItem(String name, String desc, int price, int imageResource) {
        this.image_resource = imageResource;
        this.name = name;
        this.price = price;
        this.desc = desc;
        this.quantity = 0;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getPrice() {
        return price;
    }

    public void setPrice(int price) {
        this.price = price;
    }

    public String getDesc() {
        return desc;
    }

    public void setDesc(String desc) {
        this.desc = desc;
    }

    public int getImage_resource() {
        return image_resource;
    }

    public void setImage_resource(int image_resource) {
        this.image_resource = image_resource;
    }

    public int getQuantity() {
        return quantity;
    }

    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }
}