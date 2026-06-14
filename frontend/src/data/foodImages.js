/** Local food images — served from /public/images/menu/ */

export const FOOD_IMAGES = {
  biryani: '/images/menu/biryani.jpg',
  biryaniMutton: '/images/menu/biryani-2.jpg',
  biryaniHyderabadi: '/images/menu/biryani-3.jpg',
  seafood: '/images/menu/seafood.jpg',
  rice: '/images/menu/rice.jpg',
  curry: '/images/menu/curry.jpg',
  curdRice: '/images/menu/yogurt.jpg',
  dosa: '/images/menu/dosa.jpg',
  idli: '/images/menu/idli.jpg',
  meals: '/images/menu/meals.jpg',
  northIndian: '/images/menu/north-indian.jpg',
  paneer: '/images/menu/paneer.jpg',
  snacks: '/images/menu/snacks.jpg',
  samosa: '/images/menu/samosa.jpg',
  chicken: '/images/menu/chicken.jpg',
  coffee: '/images/menu/coffee.jpg',
  drinks: '/images/menu/drinks.jpg',
  dessert: '/images/menu/dessert.jpg',
};

export const CATEGORY_FALLBACK = {
  'Rice & Biryani': FOOD_IMAGES.biryani,
  'South Indian': FOOD_IMAGES.dosa,
  'North Indian': FOOD_IMAGES.northIndian,
  'Snacks & Tiffin': FOOD_IMAGES.samosa,
  Beverages: FOOD_IMAGES.coffee,
};
