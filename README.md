# RandomTextView
滚动显示TextView的数字,支持自定义每个字符速度。
  最近在掘金这个干货平台上发了几篇博文，[这里进入我的掘金主页](http://gold.xitu.io/user/580f1397da2f60004f422d18)。

看掘金APP中文章数据的数字滚动起来很动感，效果很棒，

于是决定把它通过自定义View编写出来，方便自己和大家调用。

本文Github代码链接 
https://github.com/AndroidMsky/RandomTextView

作者博客地址：
http://blog.csdn.net/androidmsky?viewmode=list

先看看掘金的效果：

![这里写图片描述](http://img.blog.csdn.net/20161102161400896)


我们自己实现的效果：

![这里写图片描述](http://img.blog.csdn.net/20161103091026709)



感觉可以一用，一定要star我哦～


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

原理
--
用TextView去绘制10（maxLine可设置）行文字，调用canvas.drawText去绘制出来，在绘制的Y坐标不断增加便宜量，去改变绘制的高度，通过handler.postDelayed(this, 20);不断增加便宜量，并且不断判断所有位数字最后一行绘制完毕的时候，结束handler的循环调用。

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
自一次进入onDraw方法时，做了如下几件事情：
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
