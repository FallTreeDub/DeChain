/**
 * DeChainのチュートリアルの画面描画にかかわるクラス群。
 * <p>
 *     チュートリアルの表示内容を持っているのは{@link jp.kozu_osaka.android.kozuzen.tutorial.group.TutorialFragmentGroup}のサブクラスで、
 *     フィールドとして{@code firstContent}、{@code midstContents}、{@code endContent}を{@link jp.kozu_osaka.android.kozuzen.tutorial.fragment.TutorialContent}型で持っている。
 * </p>
 * <pre>
 * public class TutorialFragmentGroup {
 *
 *      &#064;StringRes
 *      private final int tutorialGroupTitleID;
 *
 *      private final TutorialContent firstContent;
 *      private final TutorialContent[] midstContents;
 *      private final TutorialContent endContent;
 *      ...</pre>
 * <p>
 *      {@link jp.kozu_osaka.android.kozuzen.tutorial.fragment.TutorialContent}は表示内容のLayout IDのみをフィールドとして持っている。
 * </p>
 * <p>
 *     {@link jp.kozu_osaka.android.kozuzen.tutorial.group.TutorialFragmentGroup#getContents()}でそれぞれの{@link jp.kozu_osaka.android.kozuzen.tutorial.fragment.TutorialContent}の
 *     表示内容をそれぞれ{@link jp.kozu_osaka.android.kozuzen.tutorial.fragment.FrameFragment}にあてはめたものを{@code Fragment[]}型で返す。
 * </p>
 * <p>
 *     {@code firstContent}は{@link jp.kozu_osaka.android.kozuzen.tutorial.fragment.FirstFrameFragment}、
 *     {@code midstContents}は{@link jp.kozu_osaka.android.kozuzen.tutorial.fragment.MidstFrameFragment}、
 *     {@code endContent}は{@link jp.kozu_osaka.android.kozuzen.tutorial.fragment.EndFrameFragment}の持つ{@link android.widget.FrameLayout}にあてはめられる。
 * </p>
 * <p>
 *     表示時は、{@link jp.kozu_osaka.android.kozuzen.tutorial.TutorialViewActivity}が{@link jp.kozu_osaka.android.kozuzen.tutorial.group.TutorialFragmentGroup#getContents()}の
 *     {@code Fragment[]}を表示する。
 * </p>
 */
package jp.kozu_osaka.android.kozuzen.tutorial;