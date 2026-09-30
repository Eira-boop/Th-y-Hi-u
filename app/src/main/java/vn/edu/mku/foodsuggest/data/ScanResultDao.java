package vn.edu.mku.foodsuggest.data;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;

import java.util.List;

/**
 * Data Access Object cho lịch sử quét nguyên liệu
 */
@Dao
public interface ScanResultDao {

    @Insert
    void insert(ScanResult result);

    @Query("SELECT * FROM scan_results ORDER BY timestamp DESC")
    List<ScanResult> getAll();

    @Query("DELETE FROM scan_results WHERE id = :id")
    void deleteById(int id);

    @Query("DELETE FROM scan_results")
    void deleteAll();

    @Query("SELECT category as label, COUNT(*) as count FROM scan_results GROUP BY category")
    List<LabelCount> getStatsByCategory();

    class LabelCount {
        public String label;
        public int count;
    }
}
