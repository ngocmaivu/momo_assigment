package org.momo;

import org.momo.service.AuthService;
import org.momo.model.Bill;
import org.momo.model.Payment;
import org.momo.model.User;
import org.momo.service.AccountService;
import org.momo.service.BillService;
import org.momo.service.PaymentService;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.List;
import java.util.Scanner;
import java.util.stream.Collectors;

public class Main {
    private static final DateTimeFormatter F = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    public static void main(String[] args) {
        AuthService auth = new AuthService();
        BillService billService = new BillService();
        AccountService account = new AccountService();

        // preload some bills (global)
        billService.addBill(new Bill("ELECTRIC", 1L, "EVN HCMC", 200_000L, LocalDate.parse("25/10/2020", F), "NOT_PAID"));
        billService.addBill(new Bill("WATER", 2L, "SAVACO HCMC", 175_000L, LocalDate.parse("30/10/2020", F), "NOT_PAID"));
        billService.addBill(new Bill("INTERNET", 3L, "VNPT", 800_000L, LocalDate.parse("30/11/2020", F), "NOT_PAID"));

        Scanner sc = new Scanner(System.in);
        // Authentication loop
        while (true) {
            System.out.println("Auth: type REGISTER <username> <password>  or  LOGIN <username> <password>  or  EXIT");
            System.out.print("auth> ");
            String line = sc.nextLine();
            if (line == null) break;
            line = line.trim();
            if (line.isEmpty()) continue;
            String[] parts = line.split("\\s+");
            String cmd = parts[0].toUpperCase();
            if ("EXIT".equals(cmd)) {
                System.out.println("Bye.");
                return;
            }
            if ("REGISTER".equals(cmd)) {
                if (parts.length < 3) {
                    System.out.println("Usage: REGISTER <username> <password>");
                    continue;
                }
                boolean ok = auth.register(parts[1], parts[2]);
                System.out.println(ok ? "Registered. You can LOGIN now." : "Register failed. Username may exist or invalid.");
                continue;
            }
            if ("LOGIN".equals(cmd)) {
                if (parts.length < 3) {
                    System.out.println("Usage: LOGIN <username> <password>");
                    continue;
                }
                boolean ok = auth.login(parts[1], parts[2]);
                if (!ok) {
                    System.out.println("Login failed.");
                    continue;
                }
                System.out.println("Login successful. Welcome " + parts[1]);
                // Enter billpay shell for this user
                runBillPayShell(sc, auth, billService, account);
                // when user logs out, continue to auth prompt
                continue;
            }
            System.out.println("Unknown auth command.");
        }
    }

    private static void runBillPayShell(Scanner sc, AuthService auth, BillService billService, AccountService account) {
        User user = auth.getCurrentUser().orElseThrow();
        PaymentService paymentService = new PaymentService(account, billService);

        System.out.println("Welcome to BillPay shell. Type HELP for commands. Type LOGOUT to leave.");
        while (true) {
            System.out.print(user.getUsername() + "> ");
            String line = sc.nextLine();
            if (line == null) break;
            line = line.trim();
            if (line.isEmpty()) continue;
            if ("LOGOUT".equalsIgnoreCase(line)) {
                auth.logout();
                System.out.println("Logged out.");
                break;
            }
            if ("EXIT".equalsIgnoreCase(line)) {
                System.out.println("Bye.");
                System.exit(0);
            }
            executeCommand(line, account, billService, paymentService);
        }
    }

    private static void executeCommand(String line, AccountService account, BillService billService, PaymentService paymentService) {
        String[] parts = line.split("\\s+");
        String cmd = parts[0].toUpperCase();
        try {
            switch (cmd) {
                case "HELP":
                    printHelp();
                    break;
                case "CASH_IN":
                    if (parts.length < 2) {
                        System.out.println("Usage: CASH_IN <amount>");
                        break;
                    }
                    long amount = Long.parseLong(parts[1]);
                    account.deposit(amount);
                    paymentService.processDueScheduledPayments();
                    System.out.println("Your available balance: " + account.getBalance());
                    break;
                case "LIST_BILL":
                    billService.listBills().forEach(b ->
                            System.out.println(formatBillLine(b)));
                    break;
                case "CREATE_BILL":
                    if (parts.length < 6) {
                        System.out.println("Usage: CREATE_BILL <id> <type> <provider> <amount> <dd/MM/yyyy>");
                        break;
                    }
                    long newId = Long.parseLong(parts[1]);
                    String type = parts[2];
                    String provider = parts[3];
                    long amt = Long.parseLong(parts[4]);
                    LocalDate due = LocalDate.parse(parts[5], F);
                    Bill b = new Bill(type, newId, provider, amt, due, "NOT_PAID");
                    billService.addBill(b);
                    System.out.println("Created bill " + newId);
                    break;
                case "DELETE_BILL":
                    if (parts.length < 2) {
                        System.out.println("Usage: DELETE_BILL <id>");
                        break;
                    }
                    long delId = Long.parseLong(parts[1]);
                    boolean removed = billService.removeBill(delId);
                    if (removed) System.out.println("Deleted bill " + delId);
                    else System.out.println("Bill not found: " + delId);
                    break;
                case "UPDATE_BILL":
                    if (parts.length < 6) {
                        System.out.println("Usage: UPDATE_BILL <id> <type> <provider> <amount> <dd/MM/yyyy>");
                        break;
                    }
                    long upId = Long.parseLong(parts[1]);
                    String upType = parts[2];
                    String upProvider = parts[3];
                    long upAmount = Long.parseLong(parts[4]);
                    LocalDate upDue = LocalDate.parse(parts[5], F);
                    boolean updated = billService.updateBill(upId, upType, upProvider, upAmount, upDue);
                    if (updated) System.out.println("Updated bill " + upId);
                    else System.out.println("Bill not found: " + upId);
                    break;
                case "VIEW_BILL":
                    if (parts.length < 2) {
                        System.out.println("Usage: VIEW_BILL <id>");
                        break;
                    }
                    long viewId = Long.parseLong(parts[1]);
                    billService.findById(viewId).ifPresentOrElse(
                            bill -> System.out.println(formatBillLine(bill)),
                            () -> System.out.println("Bill not found: " + viewId));
                    break;
                case "PAY":
                    if (parts.length < 2) {
                        System.out.println("Usage: PAY <id> [<id> ...]");
                        break;
                    }
                    List<Long> ids = Arrays.stream(parts).skip(1).map(Long::parseLong).collect(Collectors.toList());
                    boolean ok = paymentService.payBills(ids);
                    if (ok) {
                        System.out.println("Payment has been completed for Bill(s) " + ids);
                        System.out.println("Your current balance is: " + account.getBalance());
                    }
                    break;
                case "DUE_DATE":
                    billService.listUnpaidOrderedByDue().forEach(b2 -> System.out.println(formatBillLine(b2)));
                    break;
                case "SCHEDULE":
                    if (parts.length < 3) {
                        System.out.println("Usage: SCHEDULE <billId> <dd/MM/yyyy>");
                        break;
                    }
                    long billId = Long.parseLong(parts[1]);
                    LocalDate when = LocalDate.parse(parts[2], F);
                    boolean scheduled = paymentService.schedulePayment(billId, when);
                    if (scheduled) {
                        System.out.println("Payment for bill id " + billId + " is scheduled on " + parts[2]);
                    } else {
                        System.out.println("Cannot schedule. Bill not found or already paid.");
                    }
                    break;
                case "LIST_PAYMENT":
                    paymentService.listPayments().forEach(p -> System.out.println(formatPaymentLine(p)));
                    break;
                case "SEARCH_BILL_BY_PROVIDER":
                    if (parts.length < 2) {
                        System.out.println("Usage: SEARCH_BILL_BY_PROVIDER <provider>");
                        break;
                    }
                    String providerSearch = line.substring(line.indexOf(' ') + 1).trim();
                    billService.searchByProvider(providerSearch).forEach(b2 -> System.out.println(formatBillLine(b2)));
                    break;
                case "SEARCH_BILL_BY_TYPE":
                    if (parts.length < 2) {
                        System.out.println("Usage: SEARCH_BILL_BY_TYPE <type>");
                        break;
                    }
                    String typeQuery = line.substring(line.indexOf(' ') + 1).trim();
                    billService.searchByType(typeQuery).forEach(b2 -> System.out.println(formatBillLine(b2)));
                    break;
                default:
                    System.out.println("Unknown command. Type HELP.");
            }
        } catch (NumberFormatException nfe) {
            System.out.println("Invalid number format.");
        } catch (Exception e) {
            System.out.println("Error: " + e.getMessage());
        }
    }

    private static String formatBillLine(Bill b) {
        return String.format("%d. %s %d %s %s %s",
                b.getId(),
                b.getType(),
                b.getAmount(),
                b.getDueDate().format(F),
                b.getState(),
                b.getProvider());
    }

    private static String formatPaymentLine(Payment p) {
        return String.format("%d. %d %s %s %d", p.getId(), p.getAmount(), p.getPaymentDate().format(F), p.getState(), p.getBillId());
    }

    private static void printHelp() {
        System.out.println("Commands:");
        System.out.println("CASH_IN <amount>");
        System.out.println("LIST_BILL");
        System.out.println("CREATE_BILL <id> <type> <provider> <amount> <dd/MM/yyyy>");
        System.out.println("DELETE_BILL <id>");
        System.out.println("UPDATE_BILL <id> <type> <provider> <amount> <dd/MM/yyyy>");
        System.out.println("VIEW_BILL <id>");
        System.out.println("PAY <id> [<id> ...]");
        System.out.println("DUE_DATE");
        System.out.println("SCHEDULE <billId> <dd/MM/yyyy>");
        System.out.println("LIST_PAYMENT");
        System.out.println("SEARCH_BILL_BY_PROVIDER <provider>");
        System.out.println("SEARCH_BILL_BY_TYPE <type>");
        System.out.println("LOGOUT");
        System.out.println("EXIT");
    }
}