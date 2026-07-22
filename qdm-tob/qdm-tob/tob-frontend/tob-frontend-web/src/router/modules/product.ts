import type { RouteRecordRaw } from 'vue-router'

/**
 * 商品管理模块路由
 */
export const productRoutes: RouteRecordRaw = {
  path: 'product',
  redirect: '/product/list',
  meta: { title: '商品管理', icon: 'Goods' },
  children: [
    {
      path: 'list',
      name: 'ProductList',
      component: () => import('@/views/product/list/index.vue'),
      meta: { title: '商品主档', requiresAuth: false },
    },
    {
      path: 'category',
      name: 'ProductCategory',
      component: () => import('@/views/product/category/index.vue'),
      meta: { title: '商品分类', requiresAuth: false },
    },
    {
      path: 'catalog',
      name: 'ProductCatalog',
      component: () => import('@/views/product/catalog/index.vue'),
      meta: { title: '商品资料', requiresAuth: true },
    },
    {
      path: 'price',
      name: 'ProductPrice',
      component: () => import('@/views/product/price/index.vue'),
      meta: { title: '商品价格管理', requiresAuth: true },
    },
    // 后续商品管理子模块在此追加
  ],
}
