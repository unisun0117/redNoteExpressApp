// postcss.config.js
export default {
  plugins: {
    tailwindcss: {},
    autoprefixer: {},
    // 将 tailwind 的 rem 转换为小程序的 rpx
    'postcss-rem-to-responsive-pixel': {
      rootValue: 32, // 1rem = 32rpx
      propList: ['*'],
      transformUnit: 'rpx',

      // 明确指定：只转换带有 'rem' 单位的值，绝对不触碰已经是 'rpx' 或 'px' 的值
      // unitToConvert: 'rem',
    },
    'disableLaunchEditor': true
  },
}
