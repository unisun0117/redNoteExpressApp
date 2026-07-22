declare module 'element-china-area-data' {
  interface AreaItem {
    value: string
    label: string
    children?: AreaItem[]
  }
  export const regionData: AreaItem[]
  export const provinceAndCityData: AreaItem[]
  export const pcaTextArr: AreaItem[]
  export const pcTextArr: AreaItem[]
  export function codeToText(code: string): string
}
