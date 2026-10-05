export const formatCurrency = (
  amount: number,
  currency = "ZAR",
  locale = "en-ZA"
): string =>
  new Intl.NumberFormat(locale, {
    style: "currency",
    currency,
    minimumFractionDigits: 2,
  }).format(amount);