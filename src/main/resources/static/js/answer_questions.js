$(document).ready(function () {
  // Display the math problem
  var mathProblem = "c = \\pm\\sqrt{a^2 + b^2}";
  $("#math-problem-box").html(
    katex.renderToString(mathProblem, { throwOnError: false }),
  );

  // Timer functionality
  var timeLeft = 60;
  var timerId = setInterval(countdown, 1000);

  function countdown() {
    if (timeLeft == 0) {
      clearTimeout(timerId);
      // Handle time-out scenario
    } else {
      $("#time-left").text(timeLeft);
      timeLeft--;
    }
  }

  // Handle answer submission
  $("#submit-answer").click(function () {
    var userAnswer = $("#answer").val();
    // Add logic to check the answer and update the score
  });
});
