// Add these imports at the top
import com.vaultscale.event.producer.KafkaDomainEventPublisher;
import java.util.Map;

// Add this field alongside the others (Lombok's @RequiredArgsConstructor handles it)
private final KafkaDomainEventPublisher eventPublisher;

// Inside login(), right before "return AuthResponse.builder()..." add:
eventPublisher.publish("USER_LOGGED_IN", null, user.getId(), Map.of("email", user.getEmail()));
