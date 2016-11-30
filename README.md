	本文已经在微信公众号【Android群英传】独家发表。
	

未经允许不得转载。
转载请注明作者AndroidMsky及原文链接
http://blog.csdn.net/androidmsky/article/details/53009886
本文Github代码链接 
https://github.com/AndroidMsky/RandomTextView

2016年11-30号，一位热心同学私信我反映会出现内存泄漏问题。特别推出v1.2检测并且，解决内存泄漏问题，并讲述一下，看过本文的直接点传送门。

[2.v1.2更新内容](#2)

Github代码已经更新为v1.2


2016年11月11号，RandomTextView第一次更新为v1.1版本吧。
(解决了这样一个场景，一个抽奖的页面想滚动30秒，可能maxline加到100行的数字滚动，对此我要对性能进行优化避免过度绘制,在本文最后做出解释)

Github代码已经更新为v1.1


[1.v1.1更新内容](#1)

先看看X金APP的效果：

![这里写图片描述](http://img.blog.csdn.net/20161102161400896)


我们自己实现的效果：


![这里写图片描述](http://img.blog.csdn.net/20161102161502895)

接下来介绍一下我的自定义View RandomTextView的用法和原理

用法
--
考入

[RandomTextView.java](https://github.com/AndroidMsky/RandomTextView/tree/master/app/src/main/java/com/example/liangmutian/randomtextview/view)

只有200行绝对轻量方便。

xml中定义：

```
<com.example.liangmutian.randomtextview.view.RandomTextView
        android:id="@+id/rtv"
        android:layout_width="wrap_content"
        android:layout_height="wrap_content"
        android:layout_centerHorizontal="true"
        android:layout_centerVertical="true"
        android:padding="0px"
        android:text="123456"
        android:textSize="28sp"/>
```

很开心的事，RandomTextView继承自TextView所以可以使用TextView的所有方法。color，size等等直接去定义就OK啦。

所有位数相同速度滚动：
```
mRandomTextView.setText("876543");
mRandomTextView.setPianyilian(RandomTextView.ALL);
mRandomTextView.start();
```
从左到右侧由快到慢滚动：

```
mRandomTextView.setText("12313288");
mRandomTextView.setPianyilian(RandomTextView.FIRSTF_FIRST);
mRandomTextView.start();

```
从左到右侧由慢到快滚动：

```
mRandomTextView.setText("9078111123");
mRandomTextView.setPianyilian(RandomTextView.FIRSTF_LAST);
mRandomTextView.start();
```
自定义每位数字的速度滚动（每帧滚动的像素）：

```
mRandomTextView.setText("909878");
        pianyiliang[0] = 7;
        pianyiliang[1] = 6;
        pianyiliang[2] = 12;
        pianyiliang[3] = 8;
        pianyiliang[4] = 18;
        pianyiliang[5] = 10;
        mRandomTextView.setPianyilian(pianyiliang);
        mRandomTextView.start();
```
自定义滚动行数（默认10行）：

```
mRandomTextView.setMaxLine(20);
```
放置泄漏
```
    @Override
    protected void onDestroy() {
        super.onDestroy();
        mRandomTextView.destroy();
    }
```

原理
--
用TextView去绘制10（maxLine可设置）行文字，调用canvas.drawText去绘制出来，在绘制的Y坐标不断增加便宜量，去改变绘制的高度，通过handler.postDelayed(this, 20);不断增加偏移量，并且不断判断所有位数字最后一行绘制完毕的时候，结束handler的循环调用。

需要的变量：

```
//高位快
    public static final int FIRSTF_FIRST = 0;
    //高位慢
    public static final int FIRSTF_LAST = 1;
    //速度相同
    public static final int ALL = 2;
    //用户自定义速度
    public static final int USER = 3;
    //偏移速度类型
    private int pianyiliangTpye;

    //   滚动总行数 可设置
    private int maxLine = 10;
    //   当前字符串长度
    private int numLength = 0;
    //   当前text
    private String text;


    //滚动速度数组
    private int[] pianyilianglist;
    //总滚动距离数组
    private int[] pianyiliangSum;
    //滚动完成判断
    private int[] overLine;

    private Paint p;
    //第一次绘制
    private boolean firstIn = true;
    //滚动中
    private boolean auto = true;

    //text int值列表
    private ArrayList<Integer> arrayListText;

    //字体宽度
    private float f0;

    //基准线
    private int baseline;
```

OnDraw方法：

```
@Override
    protected void onDraw(Canvas canvas) {

        if (firstIn) {
            firstIn = false;
            super.onDraw(canvas);
            p = getPaint();
            Paint.FontMetricsInt fontMetrics =                p.getFontMetricsInt();
            baseline = (getMeasuredHeight() - fontMetrics.bottom + fontMetrics.top) / 2 - fontMetrics.top;
            float[] widths = new float[4];
            p.getTextWidths("9999", widths);
            f0 = widths[0];
            invalidate();
        }
        drawNumber(canvas);

```
第一次进入onDraw方法时，做了如下几件事情：
**1.**去获取当前正确的画笔p = getPaint();从而保证xml中配置的大小颜色等有效。
**2.**通过当前画笔去计算正确的drawText基准线。
            baseline = (getMeasuredHeight() - fontMetrics.bottom + fontMetrics.top) / 2 - fontMetrics.top;
 **3.**等到数字的宽度。方便横向绘制。
 p.getTextWidths("9999", widths);f0 = widths[0];
 **4.**直接通知view重绘。
 invalidate();

我们自己的绘制drawNumber方法：

```
private void drawNumber(Canvas canvas) {

        for (int j = 0; j < numLength; j++) {

            for (int i = 1; i < maxLine; i++) {


                if (i == maxLine - 1 && i * baseline + pianyiliangSum[j] <= baseline)

                {
                    pianyilianglist[j] = 0;
                    overLine[j] = 1;
                    int auto = 0;
                    for (int k = 0; k < numLength; k++) {
                        auto += overLine[k];
                    }
                    if (auto == numLength * 2 - 1) {
                        this.auto = false;
                        handler.removeCallbacks(task);
                        invalidate();
                    }

                }
                if (overLine[j] == 0)

                    canvas.drawText(setBack(arrayListText.get(j), maxLine - i - 1) + "", 0 + f0 * j,
                            i * baseline + pianyiliangSum[j], p);

                else {
                    //定位后画一次就好啦
                    if (overLine[j] == 1) {
                        overLine[j]++;
                        canvas.drawText(arrayListText.get(j) + "", 0 + f0 * j,
                                baseline, p);
                    }

                    //break;
                }}
            }}
```
这里逻辑想对复杂时间复杂度达到了O（绘制行数＊字符串位数），是个双重循环的绘制。
第一层我们称之为J循环，J循环每次循环的内容是绘制一列。
第二层循环称之为I循环，I循环负责绘制每行的每一个字符。

每次进入I循环的第一件事情是检查当前字符位，是不是最后一个

```
if (i == maxLine - 1 && i * baseline + pianyiliangSum[j] <= baseline)
```
如果是，则归零便宜量，修改标志位
```
pianyilianglist[j] = 0;
overLine[j] = 1;
```
之后去判段所有字符位是否全部绘制到最后一个：

```
int auto = 0;
for (int k = 0; k < numLength; k++) {
auto += overLine[k];}
if (auto == numLength * 2 - 1) {
this.auto = false;
handler.removeCallbacks(task);
invalidate();}
```
如果是则讲自动循环刷新的方法取消掉，并且通知view进行最后一次定位绘制。
以上就是进入i循环先对是否绘制结束的判断。

如果没有结束那么继续绘制：

```
if (overLine[j] == 0)

                    canvas.drawText(setBack(arrayListText.get(j), maxLine - i - 1) + "", 0 + f0 * j,i * baseline +pianyiliangSum[j], p);
else {
if (overLine[j] == 1) {
//定位后画一次就好啦
overLine[j]++;
canvas.drawText(arrayListText.get(j) + "", 0 + f0 * j,
baseline, p);
}
                }
```

overLine［j］中的值的意思为：0表示还没绘制到最后一行，1表示为绘制到最后一行没有进行最后的定位绘制，2表示已经进行了定位绘制。

可能对于初学者最难的就是drawText的坐标问题，x坐标比较简单
就是字符的宽度并且随着循环去变化：
```
0 + f0 * j
```
Y坐标就是当前行的基准值＋上当前便宜量：

```
i * baseline + pianyiliangSum[j]
```

每隔20毫秒去计算当前便宜量并通知刷新view：

```
private final Runnable task = new Runnable() {

        public void run() {
            // TODO Auto-generated method stub
            if (auto) {
                handler.postDelayed(this, 20);

                for (int j = 0; j < numLength; j++) {
                    pianyiliangSum[j] -= pianyilianglist[j];

                }
                invalidate();
            }

        }
    };
```
帮助计算9上面的是几。8上面是几

```
//设置上方数字0-9递减
    private int setBack(int c, int back) {

        if (back == 0) return c;

        back = back % 10;

        int re = c - back;

        if (re < 0) re = re + 10;

        return re;
    }
```

讲字符串转换为INT数组：

```
 private ArrayList<Integer> getList(String s) {

        ArrayList<Integer> arrayList = new ArrayList<Integer>();

        for (int i = 0; i < s.length(); i++) {

            String ss = s.substring(i, i + 1);

            int a = Integer.parseInt(ss);

            arrayList.add(a);
        }
        return arrayList;

    }
```
<h2 id="2">v1.2更新内容</h2>
v1.2更新内容：
解决内存泄漏问题，
看到泄可能有点手抖，不过面对现实。
上图：

![这里写图片描述](http://img.blog.csdn.net/20161130174054510)

如果反复选择屏幕让Activty重新创建，就会出现内存泄漏，安利给大家内存泄漏检测工具：leakcanary：https://github.com/square/leakcanary
配置十分简单先是引用：（2016.11.30版本）

```
 debugCompile 'com.squareup.leakcanary:leakcanary-android:1.5'
    releaseCompile 'com.squareup.leakcanary:leakcanary-android-no-op:1.5'
    testCompile 'com.squareup.leakcanary:leakcanary-android-no-op:1.5'
```
然后：

```
public class MyApplication extends Application {

    @Override
    public void onCreate() {
        super.onCreate();
        if (LeakCanary.isInAnalyzerProcess(this)) {
            // This process is dedicated to LeakCanary for heap analysis.
            // You should not init your app in this process.
            return;
        }
        LeakCanary.install(this);
    }
}

```
如果检测activity的泄漏问题，可以开启旋转屏幕一旋转就重新创建activity了，这样就反反复复创建activity。如上图泄漏问题就会被推送出来，而且明确告诉你是什么样一个引用链导致的泄漏。工具很强大有么有。本文框架的问题就是，如果RandomTextview的动画没有停止，那么activity就不会被释放掉，这样就造成了泄漏，所以在activity中写入：

```

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mRandomTextView.destroy();
    }
```
并且提供destroy方法：

```
 public void destroy (){
        auto=false;
        handler.removeCallbacks(task);

    }
```

欢迎大家提出各种问题，让控件越来越好用谢谢。
	2016.11.30 Androidmsky


<h2 id="1">v1.1更新内容</h2>
v1.1更新内容：

之前我们的思路是按照maxLine画出每一行，但是我们最多看见2行内容，这样是不科学的，完全中了过度绘制的圈套呀，再想如下一个场景，一个抽奖的页面想滚动30秒，可能maxline加到100行的数字滚动，那每帧都要绘制100行的text这显然会出现性能问题，造成掉帧的影响，所以我们队drawtext方法进行一下拦截，新建一个drawText方法：

```
private void drawText(Canvas mCanvas,String text,float x,float y,Paint p){

        if (y>=-measuredHeight&&y<=2*measuredHeight)

        mCanvas.drawText(text + "", x,
                y, p);
        else return;
    }
```
我们对y坐标进行判断，如果在textView上下各一个textView大小内，我们进行绘制，如果超出这个范围我们直接return，不做任何处理，这样既不影响我们的绘制逻辑又解决了过渡绘制问题。
讲原来的drawText方法替换：

```
 drawText(canvas,arrayListText.get(j) + "", 0 + f0 * j,
                                        baseline, p);
                       // canvas.drawText(arrayListText.get(j) + "", 0 + f0 * j,
                        //        baseline, p);
```
作者将持续维护该框架，也希望大家star，fork，issue。

共同做出一个更好的RandomTextView

	2016.11.11 Androidmsky



回顾
--

在自定义view的时候如果你的view是像本文一样，循环去绘制不断刷新的话，就意味着onDraw方法会随着你view的帧数不断的被调用，一秒可能被执行几十次，所以写在这里的方法，一定要小心为妙，比如一些无需每次都初始化的变量切记不可以定义在onDraw方法里，比如本文的getText();方法去获取当前TextView的内容，就要写在外面。但是可能有些方法你必须在super.onDraw(canvas)，以后才可以获取的比如getPaint();那么我们就可以加个布尔值firstIn来控制只有第一次进入onDraw方法才去执行，或者其它的只做一次的事情都可以这样去控制。

循环绘制动画效果我们一定要理清两条线，一条是每一帧绘制什么，另一条是动画结束你都绘制了什么。

第一条线应该注意你绘制的只是一个瞬间，是个不断重复执行的线。

第二条线就是无数个第一条线加上时间点共同组成的，主要就是控制每次的不同，比如本文中增加的偏移量，是数据（本文中每一个字符的坐标）的变化，去影响onDraw方法，绘制出不通的东西呈现在屏幕上。第二条线还要控制好什么时候结束所有的第一条线，也就是整个动画结束的条件，本文中的例子讲是一旦所有字符的最后一行都超过或者等于TextView的基准线，那么整个动画结束。

绘制原理的逻辑就讲完啦，RandomTextView可以投入使用啦，自定义view并不难，只要你知道安卓API能让你能干什么，你想干什么，你可能马上就知道你应该怎么做啦。

欢迎关注作者。欢迎评论讨论。欢迎拍砖。 

如果觉得这篇文章对你有帮助 欢迎打赏，

欢迎star，Fork我的github。

喜欢作者的也可以Follow。也算对作者的一种支持。 
本文Github代码链接 
https://github.com/AndroidMsky/RandomTextView

欢迎加作者自营安卓开发交流群：308372687
![这里写图片描述](http://img.blog.csdn.net/20161028111556438)



博主原创未经允许不许转载。



—————————————————————————————

作者推荐：

安卓自定义view滚动数据显示
http://blog.csdn.net/androidmsky/article/details/53009886
RecyclerView下拉刷新分页加载性能优化和Gilde配合加载三部曲
http://blog.csdn.net/androidmsky/article/details/53115818
打造企业级网络请求框架集合retrofit＋gson＋mvp
http://blog.csdn.net/androidmsky/article/details/52882722
安卓手机自动接起QQ视频秒变摄像头
http://blog.csdn.net/androidmsky/article/details/53066441

—————————————————————————————
