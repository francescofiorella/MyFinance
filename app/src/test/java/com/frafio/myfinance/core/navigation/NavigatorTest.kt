package com.frafio.myfinance.core.navigation

import androidx.navigation3.runtime.NavBackStack
import androidx.navigation3.runtime.NavKey
import com.google.common.truth.Truth.assertThat
import org.junit.Assert.assertThrows
import org.junit.Before
import org.junit.Test

private object FirstTopLevel : NavKey
private object SecondTopLevel : NavKey
private object ThirdTopLevel : NavKey
private object SubKeyA : NavKey
private object SubKeyB : NavKey
private object Orphan : NavKey

class NavigatorTest {

    private lateinit var state: NavigationState
    private lateinit var navigator: Navigator

    @Before
    fun setup() {
        val topLevelKeys = listOf(FirstTopLevel, SecondTopLevel, ThirdTopLevel)
        state = NavigationState(
            startKey = FirstTopLevel,
            topLevelStack = NavBackStack<NavKey>(FirstTopLevel),
            subStacks = topLevelKeys.associateWith { key -> NavBackStack<NavKey>(key) },
        )
        navigator = Navigator(state)
    }

    @Test
    fun startKey_isTheCurrentTopLevelKey() {
        assertThat(state.startKey).isEqualTo(FirstTopLevel)
        assertThat(state.currentTopLevelKey).isEqualTo(FirstTopLevel)
        assertThat(state.currentKey).isEqualTo(FirstTopLevel)
        assertThat(state.topLevelKeys).containsExactly(FirstTopLevel, SecondTopLevel, ThirdTopLevel)
    }

    @Test
    fun navigate_pushesOntoTheCurrentSubStack() {
        navigator.navigate(SubKeyA)

        assertThat(state.currentTopLevelKey).isEqualTo(FirstTopLevel)
        assertThat(state.subStacks[FirstTopLevel]?.last()).isEqualTo(SubKeyA)
    }

    @Test
    fun navigate_toATopLevelKey_switchesStack() {
        navigator.navigate(SecondTopLevel)

        assertThat(state.currentTopLevelKey).isEqualTo(SecondTopLevel)
        assertThat(state.currentKey).isEqualTo(SecondTopLevel)
    }

    @Test
    fun navigate_isSingleTop() {
        navigator.navigate(SubKeyA)
        assertThat(state.currentSubStack).containsExactly(FirstTopLevel, SubKeyA).inOrder()

        navigator.navigate(SubKeyA)
        assertThat(state.currentSubStack).containsExactly(FirstTopLevel, SubKeyA).inOrder()
    }

    @Test
    fun navigate_toAKeyAlreadyInTheSubStack_movesItToTheTop() {
        navigator.navigate(SubKeyA)
        navigator.navigate(SubKeyB)

        navigator.navigate(SubKeyA)

        assertThat(state.currentSubStack).containsExactly(FirstTopLevel, SubKeyB, SubKeyA).inOrder()
    }

    @Test
    fun navigate_toTheCurrentTopLevelKey_clearsItsSubStack() {
        navigator.navigate(SecondTopLevel)
        navigator.navigate(SubKeyA)
        assertThat(state.currentSubStack).containsExactly(SecondTopLevel, SubKeyA).inOrder()

        navigator.navigate(SecondTopLevel)

        assertThat(state.currentSubStack).containsExactly(SecondTopLevel).inOrder()
    }

    @Test
    fun subStack_growsWithinOneTopLevelKey() {
        navigator.navigate(SubKeyA)
        assertThat(state.currentKey).isEqualTo(SubKeyA)
        assertThat(state.currentTopLevelKey).isEqualTo(FirstTopLevel)

        navigator.navigate(SubKeyB)
        assertThat(state.currentKey).isEqualTo(SubKeyB)
        assertThat(state.currentTopLevelKey).isEqualTo(FirstTopLevel)
    }

    @Test
    fun multiStack_eachTopLevelKeyKeepsItsOwnSubStack() {
        navigator.navigate(SubKeyA)
        assertThat(state.currentKey).isEqualTo(SubKeyA)

        navigator.navigate(SecondTopLevel)
        assertThat(state.currentKey).isEqualTo(SecondTopLevel)
        assertThat(state.currentTopLevelKey).isEqualTo(SecondTopLevel)

        navigator.navigate(SubKeyB)
        assertThat(state.currentKey).isEqualTo(SubKeyB)
        assertThat(state.currentTopLevelKey).isEqualTo(SecondTopLevel)

        // Returning to the first stack finds its sub-stack intact.
        navigator.navigate(FirstTopLevel)
        assertThat(state.currentKey).isEqualTo(SubKeyA)
        assertThat(state.currentTopLevelKey).isEqualTo(FirstTopLevel)
    }

    @Test
    fun goBack_popsOneNonTopLevelKey() {
        navigator.navigate(SubKeyA)
        navigator.navigate(SubKeyB)
        assertThat(state.currentSubStack).containsExactly(FirstTopLevel, SubKeyA, SubKeyB).inOrder()

        navigator.goBack()

        assertThat(state.currentSubStack).containsExactly(FirstTopLevel, SubKeyA).inOrder()
        assertThat(state.currentKey).isEqualTo(SubKeyA)
        assertThat(state.currentTopLevelKey).isEqualTo(FirstTopLevel)
    }

    @Test
    fun goBack_fromATopLevelRoot_returnsToThePreviousStack() {
        navigator.navigate(SubKeyA)
        navigator.navigate(SecondTopLevel)
        assertThat(state.currentSubStack).containsExactly(SecondTopLevel).inOrder()

        navigator.goBack()

        assertThat(state.currentSubStack).containsExactly(FirstTopLevel, SubKeyA).inOrder()
        assertThat(state.currentKey).isEqualTo(SubKeyA)
        assertThat(state.currentTopLevelKey).isEqualTo(FirstTopLevel)
    }

    @Test
    fun goBack_popsMultipleNonTopLevelKeys() {
        navigator.navigate(SubKeyA)
        navigator.navigate(SubKeyB)

        navigator.goBack()
        navigator.goBack()

        assertThat(state.currentSubStack).containsExactly(FirstTopLevel).inOrder()
        assertThat(state.currentKey).isEqualTo(FirstTopLevel)
        assertThat(state.currentTopLevelKey).isEqualTo(FirstTopLevel)
    }

    @Test
    fun goBack_unwindsAcrossTopLevelStacks() {
        navigator.navigate(SecondTopLevel)
        navigator.navigate(SubKeyA)
        assertThat(state.currentSubStack).containsExactly(SecondTopLevel, SubKeyA).inOrder()

        navigator.navigate(ThirdTopLevel)
        navigator.navigate(SubKeyB)
        assertThat(state.currentSubStack).containsExactly(ThirdTopLevel, SubKeyB).inOrder()

        repeat(4) { navigator.goBack() }

        assertThat(state.currentSubStack).containsExactly(FirstTopLevel).inOrder()
        assertThat(state.currentKey).isEqualTo(FirstTopLevel)
        assertThat(state.currentTopLevelKey).isEqualTo(FirstTopLevel)
    }

    @Test
    fun goBack_atTheStartKey_isANoOp() {
        // Unlike nowinandroid, leaving the app from the start key is the caller's job.
        navigator.goBack()

        assertThat(state.topLevelStack).containsExactly(FirstTopLevel)
        assertThat(state.currentSubStack).containsExactly(FirstTopLevel)
        assertThat(state.currentKey).isEqualTo(FirstTopLevel)
    }

    @Test
    fun navigate_toTheStartKey_collapsesTheTopLevelStack() {
        navigator.navigate(SubKeyA)
        navigator.navigate(SecondTopLevel)
        navigator.navigate(ThirdTopLevel)
        assertThat(state.topLevelStack).containsExactly(FirstTopLevel, SecondTopLevel, ThirdTopLevel).inOrder()

        navigator.navigate(FirstTopLevel)

        assertThat(state.topLevelStack).containsExactly(FirstTopLevel)
        // Only the top-level stack collapses; the start key's own sub-stack survives.
        assertThat(state.currentSubStack).containsExactly(FirstTopLevel, SubKeyA).inOrder()
    }

    @Test
    fun reset_returnsEverySubStackToItsRoot() {
        navigator.navigate(SubKeyA)
        navigator.navigate(SecondTopLevel)
        navigator.navigate(SubKeyB)
        navigator.navigate(ThirdTopLevel)
        navigator.navigate(SubKeyA)

        state.reset()

        assertThat(state.topLevelStack).containsExactly(FirstTopLevel)
        state.subStacks.forEach { (key, stack) ->
            assertThat(stack).containsExactly(key)
        }
        assertThat(state.currentKey).isEqualTo(FirstTopLevel)
    }

    @Test
    fun currentSubStack_throwsForAnUnknownTopLevelKey() {
        state.topLevelStack.add(Orphan)

        assertThrows(IllegalStateException::class.java) { state.currentSubStack }
    }
}
