package arcana.model.activity;
import java.util.ArrayList;
import java.util.List;
public class QuizSession {
    private static final long QUIZ_DURATION_MILLIS = 60_000L; // 1 menit total buat semua soal

    private final List<QuizQuestion> questions;
    private final long deadlineMillis;
    private int currentIndex;
    private boolean failed;
    public QuizSession() {
        this.questions = buildQuestions();
        this.deadlineMillis = System.currentTimeMillis() + QUIZ_DURATION_MILLIS;
        this.currentIndex = 0;
        this.failed = false;
    }

    public long getSecondsRemaining() {
        long ms = deadlineMillis - System.currentTimeMillis();
        if (ms < 0) {
            ms = 0;
        }
        return (ms + 999) / 1000;
    }

    public boolean isTimedOut() {
        return System.currentTimeMillis() >= deadlineMillis;
    }

    private List<QuizQuestion> buildQuestions() {
        List<QuizQuestion> bank = new ArrayList<QuizQuestion>();
        bank.add(new QuizQuestion(
                "Arcane Blast berasal dari pemadatan apa lalu dilepas seperti ledakan?",
                new String[] { "Mana", "Air", "Angin", "Tanah" },
                0));
        bank.add(new QuizQuestion(
                "Apa fokus utama saat melepas sihir Arcane Blast?",
                new String[] { "Kecepatan lari", "Kestabilan fokus", "Jumlah emas", "Warna jubah" },
                1));
        bank.add(new QuizQuestion(
                "Skill kuat yang terbuka setelah lulus quiz akademi adalah?",
                new String[] { "Fire Magic", "Water Magic", "Arcane Blast", "Fishing" },
                2));
        return bank;
    }
    public QuizQuestion getCurrentQuestion() {
        if (isFinished()) {
            return null;
        }
        return questions.get(currentIndex);
    }
    public int getCurrentNumber() {
        return currentIndex + 1;
    }
    public int getTotalQuestions() {
        return questions.size();
    }
    public void answer(int chosenIndex) {
        QuizQuestion current = getCurrentQuestion();
        if (current == null) {
            return;
        }
        if (!current.isCorrect(chosenIndex)) {
            failed = true;
        }
        currentIndex++;
    }
    public boolean isFinished() {
        return currentIndex >= questions.size();
    }
    public boolean isPassed() {
        return isFinished() && !failed;
    }
}