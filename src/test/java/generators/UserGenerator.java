package api.generators;

import api.models.UserModel;
import java.util.Random;

public class UserGenerator {
    private static final Random random = new Random();

    public static UserModel getRandomUser() {
        long unique = System.currentTimeMillis() + random.nextInt(10000);
        String email = "user_" + unique + "@test.ru";
        String password = "pass" + random.nextInt(10000);
        String name = "Name" + random.nextInt(10000);
        return new UserModel(email, password, name);
    }
}