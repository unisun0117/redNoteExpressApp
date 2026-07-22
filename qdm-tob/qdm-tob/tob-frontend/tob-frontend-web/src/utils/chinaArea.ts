/**
 * 中国省市区数据工具（基于 element-china-area-data v5）
 */
import { regionData } from 'element-china-area-data'

/** 补充港澳台地区数据（element-china-area-data v5 的树结构不含此三地） */
const supplementary: typeof regionData = [
  {
    value: '810000',
    label: '香港特别行政区',
    children: [
      { value: '810100', label: '中西区' },
      { value: '810200', label: '湾仔区' },
      { value: '810300', label: '东区' },
      { value: '810400', label: '南区' },
      { value: '810500', label: '油尖旺区' },
      { value: '810600', label: '深水埗区' },
      { value: '810700', label: '九龙城区' },
      { value: '810800', label: '黄大仙区' },
      { value: '810900', label: '观塘区' },
      { value: '811000', label: '荃湾区' },
      { value: '811100', label: '屯门区' },
      { value: '811200', label: '元朗区' },
      { value: '811300', label: '北区' },
      { value: '811400', label: '大埔区' },
      { value: '811500', label: '西贡区' },
      { value: '811600', label: '沙田区' },
      { value: '811700', label: '葵青区' },
      { value: '811800', label: '离岛区' },
    ],
  },
  {
    value: '820000',
    label: '澳门特别行政区',
    children: [
      { value: '820100', label: '花地玛堂区' },
      { value: '820200', label: '花王堂区' },
      { value: '820300', label: '望德堂区' },
      { value: '820400', label: '大堂区' },
      { value: '820500', label: '风顺堂区' },
      { value: '820600', label: '嘉模堂区' },
      { value: '820700', label: '路凼填海区' },
      { value: '820800', label: '圣方济各堂区' },
    ],
  },
  {
    value: '710000',
    label: '台湾省',
    children: [
      { value: '710100', label: '台北市' },
      { value: '710200', label: '高雄市' },
      { value: '710300', label: '台南市' },
      { value: '710400', label: '台中市' },
      { value: '710500', label: '金门县' },
      { value: '710600', label: '南投县' },
      { value: '710700', label: '基隆市' },
      { value: '710800', label: '新竹市' },
      { value: '710900', label: '嘉义市' },
      { value: '711000', label: '新北市' },
      { value: '711100', label: '宜兰县' },
      { value: '711200', label: '新竹县' },
      { value: '711300', label: '桃园市' },
      { value: '711400', label: '苗栗县' },
      { value: '711500', label: '彰化县' },
      { value: '711600', label: '嘉义县' },
      { value: '711700', label: '云林县' },
      { value: '711800', label: '屏东县' },
      { value: '711900', label: '台东县' },
      { value: '712000', label: '花莲县' },
      { value: '712100', label: '澎湖县' },
    ],
  },
]

/** 完整省市区级联选项（含港澳台） */
export const chinaAreaOptions = [...regionData, ...supplementary]
