package generators;

import models.UserModel;
import java.util.Random;

public class UserGenerator {
    private static final Random random = new Random();

    public static UserModel getRandomUser() {
        long unique = System.currentTimeMillis() + random.nextInt(10000);
        String email = "user_" + unique + "@test.ru";
        String password = "123456";
        String name = "Name" + unique;
        return new UserModel(email, password, name);
    }
}