package arcana.model.activity;

public class QuizQuestion {
    private final String questionText;
    private final String[] options;
    private final int correctIndex;

    public QuizQuestion(String questionText, String[] options, int correctIndex) {
        this.questionText = questionText;
        this.options = options;
        this.correctIndex = correctIndex;
    }

    public String getQuestionText() {
        return questionText;
    }

    public String[] getOptions() {
        return options;
    }

    public boolean isCorrect(int chosenIndex) {
        return chosenIndex == correctIndex;
    }
}