package com.yourname.clinic.module.patient;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

public class PatientService {
    private final PatientRepository repository = new PatientRepository();
    private static final DateTimeFormatter DT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    public long create(String name, String gender, String birthDate, String phone, String remark) {
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("患者姓名不能为空");
        }

        LocalDateTime now = LocalDateTime.now();
        String nowStr = now.format(DT);

        Patient p = new Patient();
        p.setPatientCode(genPatientCode(now));
        p.setName(name.trim());
        p.setGender(gender);
        p.setBirthDate(birthDate);
        p.setPhone(phone);
        p.setRemark(remark);
        p.setCreatedAt(nowStr);
        p.setUpdatedAt(nowStr);

        return repository.insert(p);
    }

    public List<Patient> search(String keyword) {
        return repository.search(keyword);
    }

    public void delete(long id) {
        repository.softDelete(id, LocalDateTime.now().format(DT));
    }

    private String genPatientCode(LocalDateTime now) {
        // 简化版：P + yyyyMMddHHmmss
        return "P" + now.format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"));
    }
}