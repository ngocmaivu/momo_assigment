# BillPay — README

## Run project
1. Follow the command prompts `mvn exec:java` in the console.
2. Use your IDE's run configuration to execute the `main` method.

## Tests
- Unit tests are provided under `src/test`. Run tests with your test runner (use `mvn test` or your IDE).

## Overview
BillPay is a small interactive Java command-line application that lets users register/login, manage bills and pay them. 
Each registered user has a separate account balance. 
Bills are stored in-memory and are shared globally.
Payments may be scheduled and a basic transaction history is kept.

## General notes
- Date format: `dd/MM/yyyy` (e.g. `25/05/2026`).
- Bill ids are unique. Attempting to add a bill with an existing id will fail.
- Please config your IDE to use UTF-8 encoding for source files and console output to properly display Vietnamese characters in bill providers.

## Authentication (required before accessing BillPay)
- `REGISTER <username> <password>`
  - Create a new user.
  - Example: `REGISTER momo 12345`
- `LOGIN <username> <password>`
  - Login as an existing user. After success you enter the BillPay shell bound to that user's account.
  - Example: `LOGIN momo 12345`
- `EXIT` (from auth prompt)
  - Quit the program.

## BillPay shell commands (after login)
- `HELP`
  - Show available commands.
- `CASH_IN <amount>`
  - Deposit funds into the logged-in user's account (positive integer).
  - Processing of due scheduled payments is triggered after cash-in.
  - Example: `CASH_IN 1000000`
- `LIST_BILL`
  - List all bills (global list).
- `CREATE_BILL <id> <type> <provider> <amount> <dd/MM/yyyy>`
  - Create a new bill with the given id and fields.
  - Example: `CREATE_BILL 10 ELECTRIC "EVN HCMC" 200000 25/10/2025`
- `DELETE_BILL <id>`
  - Remove a bill from the global list.
  - Example: `DELETE_BILL 10`
- `UPDATE_BILL <id> <type> <provider> <amount> <dd/MM/yyyy>`
  - Update fields of an existing bill (non-empty/positive fields applied).
  - Example: `UPDATE_BILL 3 INTERNET VNPT 800000 30/11/2025`
- `VIEW_BILL <id>`
  - Show details for a specific bill.
  - Example: `VIEW_BILL 2`
- `PAY <id> [<id> ...]`
  - Pay one or more bills (by id). When multiple ids are provided, bills will be prioritized by earliest due date. Payment is atomic: all or nothing.
  - Example single: `PAY 1`
  - Example multiple: `PAY 2 3`
- `DUE_DATE`
  - List unpaid bills ordered by due date (earliest first).
- `SCHEDULE <billId> <dd/MM/yyyy>`
  - Schedule a payment for a bill on the given date. A `PENDING` payment entry is created; it will be processed when scheduled date <= today and funds available.
  - Example: `SCHEDULE 2 28/10/2025`
- `LIST_PAYMENT`
  - List recorded payments and scheduled entries with states (`PENDING`, `PROCESSED`, `FAILED`, ...).
- `SEARCH_BILL_BY_PROVIDER <provider>`
  - Search bills by provider (case-insensitive contains).
  - Example: `SEARCH_BILL_BY_PROVIDER VNPT`
- `SEARCH_BILL_BY_TYPE <type>`
  - Search bills by type (case-insensitive contains).
  - Example: `SEARCH_BILL_BY_TYPE ELECTRIC`
- `LOGOUT`
  - Log out current user and return to auth prompt.
- `EXIT`
  - Quit the application immediately.

## Example session
1. `REGISTER momo 12345`
2. `LOGIN momo 12345`
3. `momo> CASH_IN 1000000`
4. `momo> LIST_BILL`
5. `momo> PAY 1`
6. `momo> LIST_PAYMENT`
7. `momo> LOGOUT`
8. `auth> EXIT`

---
File: `README.md`