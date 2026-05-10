package com.productbasket.domain

enum class ProductCategory(val display: String) {
    Meat      ("Мясо"),
    Fish      ("Рыба"),
    Vegetable ("Овощи"),
    Fruit     ("Фрукты"),
    Grocery   ("Бакалея"),
    Spice     ("Специи"),
    Dairy     ("Молочное"),
    Liquid    ("Жидкости"),
    Oil       ("Масла"),
    Eggs      ("Яйца"),
    Bakery    ("Выпечка"),
    Frozen    ("Заморозка"),
    Condiment ("Соусы"),
    Nuts      ("Орехи/сухофрукты"),
    Other     ("Прочее");
}

enum class DishCategory(val display: String) {
    Soup        ("Суп"),
    Salad       ("Салат"),
    Bakery      ("Выпечка"),
    Dessert     ("Десерт"),
    MainCourse  ("Основное"),
    Vegetarian  ("Вегетарианское"),
    Garnish     ("Гарнир");
}
