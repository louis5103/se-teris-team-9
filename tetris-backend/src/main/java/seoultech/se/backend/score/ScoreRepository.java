package seoultech.se.backend.score;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;


@Repository
public interface ScoreRepository extends JpaRepository<ScoreEntity, Long> {
    @Query(
        value = "SELECT " +
                "    DENSE_RANK() OVER (ORDER BY score DESC) as rank, " +
                "    name, " +          
                "    score, " +         
                "    item_mode as itemMode, " + 
                "    created_at as createdAt " + 
                "FROM score_entity " +     
                "WHERE item_mode = :itemMode",
        countQuery = "SELECT count(*) FROM score_entity WHERE item_mode = :itemMode",
        nativeQuery = true
    )
    Page<ScoreRankDto> findRanksByItemMode(@Param("itemMode") Boolean itemMode, Pageable pageable);
}
