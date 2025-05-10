db = db.getSiblingDB("exchange_db");

db.currency_exchanges.updateOne(
  { sourceCurrency: "USD", targetCurrency: "PEN" },
  {
    $setOnInsert: {
      exchangeRate: 3.85,
    },
  },
  { upsert: true }
);

db.currency_exchanges.updateOne(
  { sourceCurrency: "EUR", targetCurrency: "USD" },
  {
    $setOnInsert: {
      exchangeRate: 1.1,
    },
  },
  { upsert: true }
);

db.currency_exchanges.updateOne(
  { sourceCurrency: "USD", targetCurrency: "EUR" },
  {
    $setOnInsert: {
      exchangeRate: 0.91,
    },
  },
  { upsert: true }
);
