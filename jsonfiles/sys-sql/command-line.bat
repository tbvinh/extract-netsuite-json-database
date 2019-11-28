sqlcmd -E -d dbLexorUSA -i import-alter-tables.sql

sqlcmd -E -d dbLexorUSA -i import-customer.sql

sqlcmd -E -d dbLexorUSA -i import-sale-order.sql

sqlcmd -E -d dbLexorUSA -i import-sale-order-item.sql

sqlcmd -E -d dbLexorUSA -i import-order-action-note.sql

sqlcmd -E -d dbLexorUSA -i import-order-messages.sql


