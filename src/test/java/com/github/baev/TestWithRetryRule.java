package com.github.baev;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TestRule;
import org.junit.runners.model.Statement;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.yandex.qatools.allure.Allure;
import ru.yandex.qatools.allure.annotations.Attachment;
import ru.yandex.qatools.allure.annotations.Step;
import ru.yandex.qatools.allure.events.ClearStepStorageEvent;
import ru.yandex.qatools.allure.events.RemoveAttachmentsEvent;

import java.util.Random;

import static junit.framework.TestCase.fail;

/**
 * @author Dmitry Baev charlie@yandex-team.ru
 *         Date: 18.03.16
 */
public class TestWithRetryRule {

    private static final Logger LOGGER = LoggerFactory.getLogger(TestWithRetryRule.class);

    @Rule
    public TestRule retryRule = (base, description) -> new Statement() {
        @Override
        public void evaluate() throws Throwable {
            Throwable e = null;
            for (int attempt = 0; attempt < 10; attempt++) {
                try {
                    /**
                     * Remove all attachments from failed test before retry.
                     */
                    Allure.LIFECYCLE.fire(new RemoveAttachmentsEvent(".*"));
                    /**
                     * Remove all steps from failed test before retry.
                     */
                    Allure.LIFECYCLE.fire(new ClearStepStorageEvent());
                    base.evaluate();
                    return;
                } catch (Throwable throwable) {
                    LOGGER.warn("Failed after {} attempts", attempt);
                    e = throwable;
                }
            }
            throw e;
        }
    };

    @Test
    public void shouldFailSometimes() throws Exception {
        int number = generateNumber();
        createAttachment("The number is " + number);
        if (number > 5) {
            fail("The number should be not less then 3 but was: " + number);
        }
    }

    @Step
    private int generateNumber() {
        return new Random().nextInt(10);
    }

    @Attachment
    private String createAttachment(String message) {
        return message;
    }
}
