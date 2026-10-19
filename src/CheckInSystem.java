import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

public class CheckInSystem {

    // ---------- คลาสเก็บข้อมูลแต่ละรายการ ----------
    static class Record {
        String empId;
        String type; // "IN" หรือ "OUT"
        LocalDateTime time;

        Record(String empId, String type) {
            this.empId = empId;
            this.type = type;
            this.time = LocalDateTime.now();
        }

        @Override
        public String toString() {
            DateTimeFormatter f = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss");
            return String.format("%-8s | %-3s | %s", empId, type, time.format(f));
        }
    }

    // ---------- โครงสร้างข้อมูล ----------
    private final Map<String, String> employees = new LinkedHashMap<>();
    private final List<Record> allRecords = new ArrayList<>();
    private final Map<String, Boolean> isCheckedIn = new HashMap<>();
    private final Stack<Record> undoStack = new Stack<>();
    private final Stack<Record> redoStack = new Stack<>();

    // ---------- จัดการพนักงาน ----------
    public void addEmployee(String id, String name) {
        if (employees.containsKey(id)) {
            System.out.println("รหัสนี้มีอยู่แล้ว (" + employees.get(id) + ")");
            return;
        }
        employees.put(id, name);
        isCheckedIn.put(id, false);
        System.out.println("เพิ่มพนักงานสำเร็จ: " + id + " - " + name);
    }

    public void showEmployees() {
        if (employees.isEmpty()) {
            System.out.println("ยังไม่มีพนักงานในระบบ");
            return;
        }
        System.out.println("--- รายชื่อพนักงาน ---");
        for (Map.Entry<String, String> e : employees.entrySet()) {
            String status = isCheckedIn.get(e.getKey()) ? "อยู่ในที่ทำงาน" : "ไม่อยู่";
            System.out.printf("%-8s %-20s [%s]%n", e.getKey(), e.getValue(), status);
        }
    }

    // ---------- เช็คอิน / เช็คเอาท์ ----------
    public void checkIn(String id) {
        if (!employees.containsKey(id)) {
            System.out.println("ไม่พบรหัสพนักงานนี้");
            return;
        }
        if (isCheckedIn.get(id)) {
            System.out.println(employees.get(id) + " เช็คอินไปแล้ว ยังไม่ได้เช็คเอาท์");
            return;
        }
        addRecord(new Record(id, "IN"));
        isCheckedIn.put(id, true);
    }

    public void checkOut(String id) {
        if (!employees.containsKey(id)) {
            System.out.println("ไม่พบรหัสพนักงานนี้");
            return;
        }
        if (!isCheckedIn.get(id)) {
            System.out.println(employees.get(id) + " ยังไม่ได้เช็คอิน");
            return;
        }
        addRecord(new Record(id, "OUT"));
        isCheckedIn.put(id, false);
    }

    private void addRecord(Record r) {
        allRecords.add(r);
        undoStack.push(r);   // push ลง Stack
        redoStack.clear();   // ทำรายการใหม่ ต้องล้าง Redo
        System.out.println("บันทึกสำเร็จ: " + r);
    }

    // ---------- Undo ----------
    public void undo() {
        if (undoStack.isEmpty()) {
            System.out.println("ไม่มีรายการให้ย้อน");
            return;
        }
        Record r = undoStack.pop();           // pop รายการล่าสุด
        allRecords.remove(r);
        redoStack.push(r);
        isCheckedIn.put(r.empId, r.type.equals("OUT")); // คืนสถานะเดิม
        System.out.println("ยกเลิกรายการ: " + r);
    }

    public void redo() {
        if (redoStack.isEmpty()) {
            System.out.println("ไม่มีรายการให้ทำซ้ำ");
            return;
        }
        Record r = redoStack.pop();
        allRecords.add(r);
        undoStack.push(r);
        isCheckedIn.put(r.empId, r.type.equals("IN"));
        System.out.println("ทำซ้ำรายการ: " + r);
    }

    // ---------- แสดงข้อมูล ----------
    public void showRecent(int n) {
        if (undoStack.isEmpty()) {
            System.out.println("ยังไม่มีกิจกรรม");
            return;
        }
        System.out.println("--- กิจกรรมล่าสุด " + n + " รายการ ---");
        int count = 0;
        for (int i = undoStack.size() - 1; i >= 0 && count < n; i--, count++) {
            System.out.println(undoStack.get(i));
        }
    }

    public void showAll() {
        if (allRecords.isEmpty()) {
            System.out.println("ยังไม่มีบันทึก");
            return;
        }
        System.out.println("--- บันทึกทั้งหมด ---");
        for (Record r : allRecords) {
            System.out.println(r);
        }
    }

    // ตรวจคนที่ยังไม่เช็คเอาท์
    public void showNotCheckedOut() {
        System.out.println("--- พนักงานที่ยังไม่เช็คเอาท์ ---");
        boolean found = false;
        for (Map.Entry<String, Boolean> e : isCheckedIn.entrySet()) {
            if (e.getValue()) {
                System.out.println(e.getKey() + " - " + employees.get(e.getKey()));
                found = true;
            }
        }
        if (!found) System.out.println("ไม่มี");
    }

    // ---------- เมนูหลัก ----------
    private static void printMenu() {
        System.out.println("\n===== ระบบเช็คอินพนักงาน =====");
        System.out.println("1. เพิ่มพนักงาน");
        System.out.println("2. ดูรายชื่อพนักงาน");
        System.out.println("3. เช็คอิน");
        System.out.println("4. เช็คเอาท์");
        System.out.println("5. Undo (ย้อนรายการล่าสุด)");
        System.out.println("6. Redo (ทำซ้ำ)");
        System.out.println("7. ดูกิจกรรมล่าสุด 5 รายการ");
        System.out.println("8. ดูบันทึกทั้งหมด");
        System.out.println("9. ดูคนที่ยังไม่เช็คเอาท์");
        System.out.println("0. ออกจากโปรแกรม");
        System.out.print("เลือกเมนู: ");
    }

    public static void main(String[] args) {
        CheckInSystem sys = new CheckInSystem();
        Scanner sc = new Scanner(System.in);

        while (true) {
            printMenu();
            String choice = sc.nextLine().trim();

            switch (choice) {
                case "1":
                    System.out.print("รหัสพนักงาน: ");
                    String id = sc.nextLine().trim();
                    System.out.print("ชื่อพนักงาน: ");
                    String name = sc.nextLine().trim();
                    if (id.isEmpty() || name.isEmpty()) {
                        System.out.println("กรุณากรอกข้อมูลให้ครบ");
                    } else {
                        sys.addEmployee(id, name);
                    }
                    break;
                case "2":
                    sys.showEmployees();
                    break;
                case "3":
                    System.out.print("รหัสพนักงานที่เช็คอิน: ");
                    sys.checkIn(sc.nextLine().trim());
                    break;
                case "4":
                    System.out.print("รหัสพนักงานที่เช็คเอาท์: ");
                    sys.checkOut(sc.nextLine().trim());
                    break;
                case "5":
                    sys.undo();
                    break;
                case "6":
                    sys.redo();
                    break;
                case "7":
                    sys.showRecent(5);
                    break;
                case "8":
                    sys.showAll();
                    break;
                case "9":
                    sys.showNotCheckedOut();
                    break;
                case "0":
                    System.out.println("ออกจากโปรแกรม");
                    sc.close();
                    return;
                default:
                    System.out.println("กรุณาเลือกเมนู 0-9");
            }
        }
    }
}