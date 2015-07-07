# Introduction #
We use a slight modification to the positive/neutral/negative voting system for code reviews. A negative vote indicates the reviewer feels a change is needed.

# Details #
The voting buttons at code.google.com say that a negative vote indicates the reviewer opposes the change. We also use a negative vote to indicate that a reviewer feels the author needs to address an issue by making a code change,  even if the overall change is good.

| **Vote** | **Flight Map definition** | **code.google.com definition** |
|:---------|:--------------------------|:-------------------------------|
| Positive | I support this change, and _**no code changes are needed**_ | I support this change          |
| Neutral  | I am ambivalent about this change | I am ambivalent about this change	|
| Negative | _**I believe code changes are needed**_, or I oppose this change and I have explained why in my review comments. | I oppose this change, and I am willing to explain why. |

# Reviewing Code #
Try to review soon after the commit to make it easier for the author to address any issues you have.

# Addressing a Review of Your code #
If someone made a negative vote on one of your revisions, address the comment either by changing the code or talking to the reviewer about it. When you've addressed the issue, add a positive vote to the revision that had the negative vote, and explain how you addressed the issue. If you made a code change and submitted it, include the revision number. Hint: if you reference your revision in the form [r287](https://code.google.com/p/flightmap/source/detail?r=287), then a link will automatically be created for you.