package com.productbasket.domain

enum class ProductCategory(val display: String, val unitCategory: UnitCategory) {
    Meat      ("Мясо",        UnitCategory.Weight),
    Fish      ("Рыба",        UnitCategory.Weight),
    Vegetable ("Овощи",       UnitCategory.Weight),
    Fruit     ("Фрукты",      UnitCategory.Weight),
    Grocery   ("Бакалея",     UnitCategory.Weight),
    Spice     ("Специи",      UnitCategory.Weight),
    Dairy     ("Молочное",    UnitCategory.Weight),
    Liquid    ("Жидкости",    UnitCategory.Volume),
    Oil       ("Масла",       UnitCategory.Volume),
    Eggs      ("Яйца",        UnitCategory.Countable),
    Bakery    ("Выпечка",     UnitCategory.Weight),
    Frozen    ("Заморозка",   UnitCategory.Weight),
    Condiment ("Соусы",       UnitCategory.Volume),
    Nuts      ("Орехи/сухофрукты", UnitCategory.Weight),
    Other     ("Прочее",      UnitCategory.Weight);
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
