package exam.repository;

import exam.model.Laptop;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Set;

@Repository
public interface LaptopRepository extends JpaRepository<Laptop, Integer> {
    Laptop findByMacAddress(String macAddress);

    @Query("Select DISTINCT l from Laptop l order by l.cpuSpeed desc,l.ram desc,l.storage desc,l.macAddress asc")
    Set<Laptop> exportBestLaptops();
}

