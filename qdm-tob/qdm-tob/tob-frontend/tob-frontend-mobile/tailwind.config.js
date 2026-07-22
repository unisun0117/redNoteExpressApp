/** @type {import('tailwindcss').Config} */
export default {
  // 1. 确保兼容所有可能的小程序文件
  content: ['./index.html', './src/**/*.{vue,js,ts,jsx,tsx,wxml,wxss}'],
  theme: {
    extend: {
      colors: {
        primary: '#E01B1B',
        secondary: '#F87171', // 辅助色 Red-400（Banner 渐变起色等）
        cta: '#CA8A04', // CTA 暖金（同一视图最多 1 个）
        // —— 业务状态色 ——
        success: '#00b578',
        warning: '#ff8f1f',
        danger: '#ff3141',
        // gray: {
        //   100: '#f7f8fa',
        //   200: '#f2f3f5',
        //   300: '#ebedf0',
        //   400: '#dcdee0',
        //   500: '#c8c9cc',
        //   600: '#969799',
        //   700: '#646566',
        //   800: '#323233',
        // },
      },
      
      // 注意：这里【不要】再写 rpx 了，全部用默认的 rem 或者纯数值。
      // 因为你的 PostCSS 插件会统一把最终生成的 rem 乘以 32 变成 rpx。
      
      // 3. 自定义字号映射（基于 1rem = 32rpx 换算）
      fontSize: {
        xs: ['0.625rem', '0.875rem'],  // [20rpx, 28rpx]
        sm: ['0.75rem', '1rem'],       // [24rpx, 32rpx]
        base: ['0.875rem', '1.25rem'], // [28rpx, 40rpx]
        lg: ['1rem', '1.375rem'],      // [32rpx, 44rpx]
        xl: ['1.125rem', '1.5rem'],    // [36rpx, 48rpx]
      },
    },
  },
  plugins: [
    // 注入一个专属的、不污染原生 text 的新工具类
    function({ matchUtilities }) {
      const rpxSizes = {};
      for (let size = 20; size <= 56; size += 2) {
        rpxSizes[`${size}rpx`] = `${size}rpx`;
      }
      matchUtilities(
        {
          'fs': (value) => {
            // 如果用户写的是 fs-20rpx，Tailwind 传进来的 value 可能是 '20rpx'
            // 如果用户写的是纯数字（如预设映射），或者是显式任意值，都能安全返回
            return {
              fontSize: value,
            };
          },
        },
        {
          // 这里的关键：允许工具类直接匹配动态的字符串单位
          // 使得 fs-20rpx 这种不带中括号的写法能够被识别为一个合法的值
          values: rpxSizes,
          // 开启任意值支持，保证 fs-[20rpx] 或者突发奇想的 fs-[23rpx] 也能用
          supportsNegativeValues: false,
        }
      )
    }
  ],
  // 4. 禁用小程序环境完全无法支持的特性
  corePlugins: {
    // 禁用默认的全局样式重置（避免污染小程序原生 view, text 等标签）
    preflight: false,
    
    // 禁用小程序不支持的现代滤镜
    backdropBlur: false,
    backdropFilter: false,
    backdropGrayscale: false,
    backdropHueRotate: false,
    backdropInvert: false,
    backdropOpacity: false,
    backdropSaturate: false,
    backdropSepia: false,
    filter: false,
    blur: false,
  },
}