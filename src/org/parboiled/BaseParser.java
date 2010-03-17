/*
 * Copyright (C) 2009 Mathias Doenitz
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.parboiled;

import com.google.common.base.Preconditions;
import org.jetbrains.annotations.NotNull;
import org.parboiled.errors.ParserRuntimeException;
import org.parboiled.matchers.*;
import org.parboiled.support.*;

import static com.google.common.collect.ObjectArrays.concat;

/**
 * Base class of all parboiled parsers. Defines the basic rule creation methods.
 *
 * @param <V> the type of the value field of the parse tree nodes created by this parser
 */
public abstract class BaseParser<V> extends BaseActions<V> {

    /**
     * Explicitly creates a rule matching the given character. Normally you can just specify the character literal
     * directly in you rule description. However, if you don't want to go through {@link #fromCharLiteral(char)},
     * e.g. because you redefined it, you can also use this wrapper.
     * <p>Note: This methods carries a {@link Cached} annotation, which means that multiple invocations with the same
     * argument will yield the same rule instance.</p>
     *
     * @param c the char to match
     * @return a new rule
     */
    @Cached
    public Rule ch(char c) {
        return new CharMatcher(c);
    }

    /**
     * Explicitly creates a rule matching the given character case-independently.
     * <p>Note: This methods carries a {@link Cached} annotation, which means that multiple invocations with the same
     * argument will yield the same rule instance.</p>
     *
     * @param c the char to match independently of its case
     * @return a new rule
     */
    @Cached
    public Rule charIgnoreCase(char c) {
        return Character.isLowerCase(c) == Character.isUpperCase(c) ? ch(c) : new CharIgnoreCaseMatcher(c);
    }

    /**
     * Creates a rule matching a range of characters from cLow to cHigh (both inclusively).
     * <p>Note: This methods carries a {@link Cached} annotation, which means that multiple invocations with the same
     * arguments will yield the same rule instance.</p>
     *
     * @param cLow  the start char of the range (inclusively)
     * @param cHigh the end char of the range (inclusively)
     * @return a new rule
     */
    @Cached
    public Rule charRange(char cLow, char cHigh) {
        return cLow == cHigh ? ch(cLow) : new CharRangeMatcher(cLow, cHigh);
    }

    /**
     * Creates a new rule that matches any of the characters in the given string.
     * <p>Note: This methods carries a {@link Cached} annotation, which means that multiple invocations with the same
     * argument will yield the same rule instance.</p>
     *
     * @param characters the characters
     * @return a new rule
     */
    public Rule charSet(@NotNull String characters) {
        return charSet(characters.toCharArray());
    }

    /**
     * Creates a new rule that matches any of the characters in the given char array.
     * <p>Note: This methods carries a {@link Cached} annotation, which means that multiple invocations with the same
     * argument will yield the same rule instance.</p>
     *
     * @param characters the characters
     * @return a new rule
     */
    @Cached
    public Rule charSet(@NotNull char... characters) {
        Preconditions.checkArgument(characters.length > 0);
        return characters.length == 1 ? ch(characters[0]) : charSet(Characters.of(characters));
    }

    /**
     * Creates a new rule that matches any of the given characters.
     * <p>Note: This methods carries a {@link Cached} annotation, which means that multiple invocations with the same
     * argument will yield the same rule instance.</p>
     *
     * @param characters the characters
     * @return a new rule
     */
    @Cached
    public Rule charSet(@NotNull Characters characters) {
        return !characters.isSubtractive() && characters.getChars().length == 1 ?
                ch(characters.getChars()[0]) : new CharactersMatcher<V>(characters);
    }

    /**
     * Explicitly creates a rule matching the given string. Normally you can just specify the string literal
     * directly in you rule description. However, if you want to not go through {@link #fromStringLiteral(String)},
     * e.g. because you redefined it, you can also use this wrapper.
     * <p>Note: This methods carries a {@link Cached} annotation, which means that multiple invocations with the same
     * argument will yield the same rule instance.</p>
     *
     * @param string the string to match
     * @return a new rule
     */
    public Rule string(@NotNull String string) {
        return string(string.toCharArray());
    }

    /**
     * Explicitly creates a rule matching the given string. Normally you can just specify the string literal
     * directly in you rule description. However, if you want to not go through {@link #fromStringLiteral(String)},
     * e.g. because you redefined it, you can also use this wrapper.
     * <p>Note: This methods carries a {@link Cached} annotation, which means that multiple invocations with the same
     * argument will yield the same rule instance.</p>
     *
     * @param characters the characters of the string to match
     * @return a new rule
     */
    @Cached
    @Leaf
    public Rule string(char... characters) {
        if (characters.length == 1) return ch(characters[0]); // optimize one-char strings
        Rule[] matchers = new Rule[characters.length];
        for (int i = 0; i < characters.length; i++) {
            matchers[i] = ch(characters[i]);
        }
        return sequence(matchers).label(
                new StringBuilder(characters.length + 2).append('"').append(characters).append('"').toString()
        );
    }

    /**
     * Explicitly creates a rule matching the given string in a case-independent fashion.
     * <p>Note: This methods carries a {@link Cached} annotation, which means that multiple invocations with the same
     * argument will yield the same rule instance.</p>
     *
     * @param string the string to match
     * @return a new rule
     */
    public Rule stringIgnoreCase(@NotNull String string) {
        return stringIgnoreCase(string.toCharArray());
    }

    /**
     * Explicitly creates a rule matching the given string in a case-independent fashion.
     * <p>Note: This methods carries a {@link Cached} annotation, which means that multiple invocations with the same
     * argument will yield the same rule instance.</p>
     *
     * @param characters the characters of the string to match
     * @return a new rule
     */
    @Cached
    @Leaf
    public Rule stringIgnoreCase(char... characters) {
        if (characters.length == 1) return charIgnoreCase(characters[0]); // optimize one-char strings
        Rule[] matchers = new Rule[characters.length];
        for (int i = 0; i < characters.length; i++) {
            matchers[i] = charIgnoreCase(characters[i]);
        }
        return sequence(matchers).label(
                new StringBuilder(characters.length + 2).append('"').append(characters).append('"').toString()
        );
    }

    /**
     * Creates a new rule that successively tries all of the given subrules and succeeds when the first one of
     * its subrules matches. If all subrules fail this rule fails as well.
     * <p>Note: This methods carries a {@link Cached} annotation, which means that multiple invocations with the same
     * arguments will yield the same rule instance.</p>
     *
     * @param rule      the first subrule
     * @param rule2     the second subrule
     * @param moreRules the other subrules
     * @return a new rule
     */
    public Rule firstOf(Object rule, Object rule2, @NotNull Object... moreRules) {
        return firstOf(concat(rule, concat(rule2, moreRules)));
    }

    /**
     * Creates a new rule that successively tries all of the given subrules and succeeds when the first one of
     * its subrules matches. If all subrules fail this rule fails as well.
     * <p>Note: This methods carries a {@link Cached} annotation, which means that multiple invocations with the same
     * argument will yield the same rule instance.</p>
     *
     * @param rules the subrules
     * @return a new rule
     */
    @Cached
    @Label
    public Rule firstOf(@NotNull Object[] rules) {
        return rules.length == 1 ? toRule(rules[0]) : new FirstOfMatcher(toRules(rules));
    }

    /**
     * Creates a new rule that tries repeated matches of its subrule and succeeds if the subrule matches at least once.
     * If the subrule does not match at least once this rule fails.
     * <p>Note: This methods carries a {@link Cached} annotation, which means that multiple invocations with the same
     * argument will yield the same rule instance.</p>
     *
     * @param rule the subrule
     * @return a new rule
     */
    @Cached
    @Label
    public Rule oneOrMore(Object rule) {
        return new OneOrMoreMatcher(toRule(rule));
    }

    /**
     * Creates a new rule that tries a match on its subrule and always succeeds, independently of the matching
     * success of its subrule.
     * <p>Note: This methods carries a {@link Cached} annotation, which means that multiple invocations with the same
     * argument will yield the same rule instance.</p>
     *
     * @param rule the subrule
     * @return a new rule
     */
    @Cached
    @Label
    public Rule optional(Object rule) {
        return new OptionalMatcher(toRule(rule));
    }

    /**
     * Creates a new rule that only succeeds if all of its subrule succeed, one after the other.
     * <p>Note: This methods carries a {@link Cached} annotation, which means that multiple invocations with the same
     * arguments will yield the same rule instance.</p>
     *
     * @param rule      the first subrule
     * @param rule2     the second subrule
     * @param moreRules the other subrules
     * @return a new rule
     */
    public Rule sequence(Object rule, Object rule2, @NotNull Object... moreRules) {
        return sequence(concat(rule, concat(rule2, moreRules)));
    }

    /**
     * Creates a new rule that only succeeds if all of its subrule succeed, one after the other.
     * <p>Note: This methods carries a {@link Cached} annotation, which means that multiple invocations with the same
     * arguments will yield the same rule instance.</p>
     *
     * @param rules the sub rules
     * @return a new rule
     */
    @Cached
    @Label
    public Rule sequence(@NotNull Object[] rules) {
        return rules.length == 1 ? toRule(rules[0]) : new SequenceMatcher(toRules(rules));
    }

    /**
     * Creates a new rule that acts as a syntactic predicate, i.e. tests the given subrule against the current
     * input position without actually matching any characters. Succeeds if the subrule succeeds and fails if the
     * subrule rails. Since this rule does not actually consume any input it will never create a parse tree node.
     * <p>Note: This methods carries a {@link Cached} annotation, which means that multiple invocations with the same
     * argument will yield the same rule instance.</p>
     *
     * @param rule the subrule
     * @return a new rule
     */
    @Cached
    public Rule test(Object rule) {
        return new TestMatcher(toRule(rule));
    }

    /**
     * Creates a new rule that acts as an inverse syntactic predicate, i.e. tests the given subrule against the current
     * input position without actually matching any characters. Succeeds if the subrule fails and fails if the
     * subrule succeeds. Since this rule does not actually consume any input it will never create a parse tree node.
     * <p>Note: This methods carries a {@link Cached} annotation, which means that multiple invocations with the same
     * argument will yield the same rule instance.</p>
     *
     * @param rule the subrule
     * @return a new rule
     */
    @Cached
    public Rule testNot(Object rule) {
        return new TestNotMatcher(toRule(rule));
    }

    /**
     * Creates a new rule that tries repeated matches of its subrule.
     * Succeeds always, even if the subrule doesn't match even once.
     * <p>Note: This methods carries a {@link Cached} annotation, which means that multiple invocations with the same
     * argument will yield the same rule instance.</p>
     *
     * @param rule the subrule
     * @return a new rule
     */
    @Cached
    @Label
    public Rule zeroOrMore(Object rule) {
        return new ZeroOrMoreMatcher(toRule(rule));
    }

    /**
     * Matches the EOI (end of input) character.
     *
     * @return a new rule
     */
    @KeepAsIs
    public Rule eoi() {
        return ch(Characters.EOI).label("EOI");
    }

    /**
     * Matches any character except {@link Characters#EOI}.
     *
     * @return a new rule
     */
    public Rule any() {
        return new CharactersMatcher<V>(Characters.allBut(Characters.EOI)).label("ANY");
    }

    /**
     * Matches nothing and therefore always succeeds.
     *
     * @return a new rule
     */
    public Rule empty() {
        return new EmptyMatcher<V>();
    }

    ///************************* "MAGIC" METHODS ***************************///

    /**
     * Changes the context scope of all arguments to the current parent scope.
     *
     * @param argument the arguments to change to context for
     * @return the result of the argument
     */
    @SuppressWarnings({"UnusedDeclaration"})
    public final <T> T UP(T argument) {
        throw new UnsupportedOperationException("UP(...) calls can only be used in rule defining parser methods");
    }

    /**
     * Changes the context scope of all arguments to the parent scope two levels up.
     * Equivalent to UP(UP(...))
     *
     * @param argument the arguments to change to context for
     * @return the result of the argument
     */
    @SuppressWarnings({"UnusedDeclaration"})
    public final <T> T UP2(T argument) {
        throw new UnsupportedOperationException("UP(...) calls can only be used in rule defining parser methods");
    }

    /**
     * Changes the context scope of all arguments to parent scope three levels up.
     * Equivalent to UP(UP(UP(...)))
     *
     * @param argument the arguments to change to context for
     * @return the result of the argument
     */
    @SuppressWarnings({"UnusedDeclaration"})
    public final <T> T UP3(T argument) {
        throw new UnsupportedOperationException("UP(...) calls can only be used in rule defining parser methods");
    }

    /**
     * Changes the context scope of all arguments to parent scope three levels up.
     * Equivalent to UP(UP(UP(UP(...))))
     *
     * @param argument the arguments to change to context for
     * @return the result of the argument
     */
    @SuppressWarnings({"UnusedDeclaration"})
    public final <T> T UP4(T argument) {
        throw new UnsupportedOperationException("UP(...) calls can only be used in rule defining parser methods");
    }

    /**
     * Changes the context scope of all arguments to the current sub scope. This will only work if this call is
     * at some level wrapped with one or more {@link #UP(Object)} calls, since the default scope is always at
     * the bottom of the context chain.
     *
     * @param argument the arguments to change to context for
     * @return the result of the argument
     */
    @SuppressWarnings({"UnusedDeclaration"})
    public final <T> T DOWN(T argument) {
        throw new UnsupportedOperationException("DOWN(...) calls can only be used in rule defining parser methods");
    }

    /**
     * Changes the context scope of all arguments to the sub scope two levels down. This will only work if this call is
     * at some level wrapped with {@link #UP(Object)} calls, since the default scope is always at
     * the bottom of the context chain.
     * Equivalent to DOWN(DOWN(...))
     *
     * @param argument the arguments to change to context for
     * @return the result of the argument
     */
    @SuppressWarnings({"UnusedDeclaration"})
    public final <T> T DOWN2(T argument) {
        throw new UnsupportedOperationException("DOWN(...) calls can only be used in rule defining parser methods");
    }

    /**
     * Changes the context scope of all arguments to the sub scope three levels down. This will only work if this call is
     * at some level wrapped with {@link #UP(Object)} calls, since the default scope is always at
     * the bottom of the context chain.
     * Equivalent to DOWN(DOWN(DOWN(...)))
     *
     * @param argument the arguments to change to context for
     * @return the result of the argument
     */
    @SuppressWarnings({"UnusedDeclaration"})
    public final <T> T DOWN3(T argument) {
        throw new UnsupportedOperationException("DOWN(...) calls can only be used in rule defining parser methods");
    }

    /**
     * Changes the context scope of all arguments to the sub scope four levels down. This will only work if this call is
     * at some level wrapped with {@link #UP(Object)} calls, since the default scope is always at
     * the bottom of the context chain.
     * Equivalent to DOWN(DOWN(DOWN(DOWN(...))))
     *
     * @param argument the arguments to change to context for
     * @return the result of the argument
     */
    @SuppressWarnings({"UnusedDeclaration"})
    public final <T> T DOWN4(T argument) {
        throw new UnsupportedOperationException("DOWN(...) calls can only be used in rule defining parser methods");
    }

    ///************************* HELPER METHODS ***************************///

    /**
     * Used internally to convert the given character literal to a parser rule.
     * You can override this method, e.g. for specifying a sequence that automatically matches all trailing
     * whitespace after the character.
     *
     * @param c the character
     * @return the rule
     */
    protected Rule fromCharLiteral(char c) {
        return ch(c);
    }

    /**
     * Used internally to convert the given string literal to a parser rule.
     * You can override this method, e.g. for specifying a sequence that automatically matches all trailing
     * whitespace after the string.
     *
     * @param string the string
     * @return the rule
     */
    protected Rule fromStringLiteral(@NotNull String string) {
        return fromCharArray(string.toCharArray());
    }

    /**
     * Used internally to convert the given char array to a parser rule.
     * You can override this method, e.g. for specifying a sequence that automatically matches all trailing
     * whitespace after the characters.
     *
     * @param array the char array
     * @return the rule
     */
    protected Rule fromCharArray(@NotNull char[] array) {
        return string(array);
    }

    /**
     * Converts the given object array to an array of rules.
     *
     * @param objects the objects to convert
     * @return the rules corresponding to the given objects
     */
    public Rule[] toRules(@NotNull Object... objects) {
        Rule[] rules = new Rule[objects.length];
        for (int i = 0; i < objects.length; i++) {
            rules[i] = toRule(objects[i]);
        }
        return rules;
    }

    /**
     * Converts the given object to a rule.
     * This method can be overriden to enable the use of custom objects directly in rule specifications.
     *
     * @param obj the object to convert
     * @return the rule corresponding to the given object
     */
    @SuppressWarnings({"unchecked"})
    public Rule toRule(Object obj) {
        if (obj instanceof Rule) return (Rule) obj;
        if (obj instanceof Character) return fromCharLiteral((Character) obj);
        if (obj instanceof String) return fromStringLiteral((String) obj);
        if (obj instanceof char[]) return fromCharArray((char[]) obj);
        if (obj instanceof Action) return new ActionMatcher<V>((Action<V>) obj);

        throw new ParserRuntimeException("'" + obj + "' cannot be automatically converted to a parser Rule");
    }

}
