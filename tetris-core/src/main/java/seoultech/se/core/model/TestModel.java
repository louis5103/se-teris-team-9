package seoultech.se.core.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Lombok 인식 테스트용 클래스
 * IDE에서 이 클래스를 열었을 때 lombok이 생성한 메서드들이 보이는지 확인
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TestModel {
    
    private String name;
    private Integer value;
    
    // IDE에서 다음 메서드들이 인식되어야 합니다:
    // - getName(), setName()
    // - getValue(), setValue()  
    // - toString(), equals(), hashCode()
    // - TestModel.builder()
    
    /**
     * IDE 테스트: 이 메서드에서 Lombok 생성 메서드를 사용해보세요
     */
    public void testLombokMethods() {
        // 다음 코드들이 IDE에서 자동완성되고 오류가 없어야 합니다:
        
        // Builder 패턴 사용
        TestModel model = TestModel.builder()
            .name("test")
            .value(42)
            .build();
            
        // Getter 사용
        String modelName = model.getName();
        Integer modelValue = model.getValue();
        
        // Setter 사용  
        model.setName("updated");
        model.setValue(100);
        
        // toString 사용
        String modelString = model.toString();
        
        System.out.println("Model: " + modelString);
    }
}
