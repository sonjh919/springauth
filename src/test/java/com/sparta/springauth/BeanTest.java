package com.sparta.springauth;

import com.sparta.springauth.food.Food;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
public class BeanTest {

    // 기본적으로 autowired는 Food type으로 bean을 찾는다. Food food 하면 오류가 난다.
    // 1. 연결되지 않을 경우, Bean에 이름을 직접 명시하여 bean을 찾는다.
    // 2. @Primary 객체를 우선으로 찾는다. 범용적으로 사용되는 객체
    // 3. @Qualifier 활용. 지엽적으로 사용되는 객체
    // Primary랑 Qualifier가 동시에 걸려있으면 Qualifier의 우선순위가 더 높다. 좁은 범위의 설정의 우선순위가 더 높다.

    @Autowired
    Food pizza; // 직접 명시

    @Autowired
    Food chicken; // 직접 명시

//    @Autowired
//    Food food;

    @Autowired
    @Qualifier("pizza")
    Food food;


    @Test
    @DisplayName("테스트")
    void test1(){
        pizza.eat();
        chicken.eat();
    }


}
