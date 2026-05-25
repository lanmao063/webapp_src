export function calcFee(weight) {
  if (!weight || weight <= 0) return 10
  if (weight <= 1) return 10
  if (weight <= 5) return 15
  if (weight <= 10) return 25
  if (weight <= 20) return 40
  return Math.round(60 + (weight - 20) * 3)
}

export const feeTable = [
  { range: '0 - 1 kg', fee: '¥10' },
  { range: '1 - 5 kg', fee: '¥15' },
  { range: '5 - 10 kg', fee: '¥25' },
  { range: '10 - 20 kg', fee: '¥40' },
  { range: '20 kg 以上', fee: '¥60 + 3/kg' }
]
