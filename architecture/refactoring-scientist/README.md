# Discovering Scientist as a helper to do large refactorings
Github’s Scientist is a tool to create fitness functions for critical path refactorings in a project. It relies on the idea that for a large enough system, the behavior or data complexity makes it harder to refactor the critical paths only with the help of tests. If we can run the new path and old path in production in parallel without affecting the current behavior, and compare the results, then we can decide the best moment to switch to the new path more confidently.

This simple example demonstrates the application of the concept on a java project.

If you are interested, you can also read the related article [Github’s Scientist as a helper to do large refactorings](https://www.entrofi.net/githubs-scientist-as-a-helper-to-do-large-refactorings/)