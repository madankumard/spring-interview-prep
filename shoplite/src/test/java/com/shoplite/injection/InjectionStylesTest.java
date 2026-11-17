package com.shoplite.injection;

import com.shoplite.notification.NotificationService;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

public class InjectionStylesTest {

    private final List<String> sent = new ArrayList<>();
    private final NotificationService fake = (to, msg) -> sent.add(msg);

    @Test
    void constructorInjection_fakePassedStraightIn() {
        new ConstructorInjectedReceiptService(fake).sendReceipt("bob@shoplite.com", "B-1");
        assertThat(sent).containsExactly("[constructor] Receipt for order B-1");
    }

    @Test
    void setterInjection_worksOnlyIfYouRememberTheSetter() {
        SetterInjectedReceiptService service = new SetterInjectedReceiptService();
        assertThatThrownBy(() -> service.sendReceipt("bob@shoplite.com", "B-2"))
            .isInstanceOf(NullPointerException.class);

        service.setNotificationService(fake);
        service.sendReceipt("bob@shoplite.com", "B-2");
        assertThat(sent).containsExactly("[setter] Receipt for order B-2");
    }

    @Test
    void fieldInjection_noWayToGiveItTheFake() {
        FieldInjectedReceiptService service = new FieldInjectedReceiptService();
        assertThatThrownBy(() -> service.sendReceipt("bob@shoplite.com", "B-3"))
                .isInstanceOf(NullPointerException.class);
    }
}
