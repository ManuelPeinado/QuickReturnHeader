QuickReturnHeader
==================

###Introduction

QuickReturnHeader is a tiny Android library that implements the popular ["quick return" design pattern][1] for lists and scrollable content in general.

In this pattern, which can be found for instance in the Google Keep app, the header hides as soon as we start scrolling down, and reappears inmediately (regardless of how far down the list we are) as soon as we scroll up again. A video is worth a thousand images:

*YouTube video coming soon*

An alternative, more feature-complete implementation of the pattern can be found [here][2]. The benefits of QuickReturnHeader are that it's easier to use, has a simpler implementation, and can be used with ScrollViews in addition to ListViews.

Please keep in mind that this pattern (despite being used by Google itself in several applications) is [somewhat controversial][3]. Please read [this][4] before you decide whether you should use it in your application.

###Sample application

A sample application showcasing the different features of the library is available:

*Google Play link coming soon*

You can browse its [source code][5] to see how easy it is to integrate QuickReturnHeader in your application.

###Including in your project

Just add the library to your application as a library project. Or if you use maven, add the following dependency to your pom:

```xml
<dependency>
    <groupId>com.github.manuelpeinado.fadingactionbar</groupId>
    <artifactId>fadingactionbar</artifactId>
    <version>2.1.0</version>
    <type>apklib</type>
</dependency>
```

###Usage


Using the library is really simple, just look at the source code of the provided samples:

* [If your content should be in a `ScrollView`][6].
* [If your content should be in a `ListView`][7].

You can even use the library [from a fragment][8], which is useful when implementing a dual phone/tablet layout.

###Who's using it

*Does your app use QuickReturnHeader? If you want to be featured on this list drop me a line.*


###Developed By

Manuel Peinado Gallego - <manuel.peinado@gmail.com>

<a href="https://twitter.com/mpg2">
  <img alt="Follow me on Twitter"
       src="https://raw.github.com/ManuelPeinado/NumericPageIndicator/master/art/twitter.png" />
</a>
<a href="https://plus.google.com/106514622630861903655">
  <img alt="Follow me on Google+"
       src="https://raw.github.com/ManuelPeinado/NumericPageIndicator/master/art/google-plus.png" />
</a>
<a href="http://www.linkedin.com/pub/manuel-peinado-gallego/1b/435/685">
  <img alt="Follow me on LinkedIn"
       src="https://raw.github.com/ManuelPeinado/NumericPageIndicator/master/art/linkedin.png" />


###License


    Copyright 2013 Manuel Peinado

    Licensed under the Apache License, Version 2.0 (the "License");
    you may not use this file except in compliance with the License.
    You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0

    Unless required by applicable law or agreed to in writing, software
    distributed under the License is distributed on an "AS IS" BASIS,
    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
    See the License for the specific language governing permissions and
    limitations under the License.


[1]: https://plus.google.com/u/0/+RomanNurik/posts/1Sb549FvpJt
[2]: https://github.com/LarsWerkman/QuickReturnListView
[3]: https://plus.google.com/104844169030193199790/posts/fKEeU4xvKvS
[4]: http://www.androiduipatterns.com/2012/08/an-emerging-ui-pattern-quick-return.html
[5]: http://www.androiduipatterns.com/2012/08/an-emerging-ui-pattern-quick-return.html
[6]: https://github.com/ManuelPeinado/QuickReturnHeader/blob/master/sample/src/com/manuelpeinado/quickreturnheader/demo/ScrollViewSampleActivity.java
[7]: https://github.com/ManuelPeinado/QuickReturnHeader/blob/master/sample/src/com/manuelpeinado/quickreturnheader/demo/ListViewSampleActivity.java