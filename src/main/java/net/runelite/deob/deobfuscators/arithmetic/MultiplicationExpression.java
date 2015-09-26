package net.runelite.deob.deobfuscators.arithmetic;

import java.util.ArrayList;
import java.util.List;
import net.runelite.deob.attributes.code.Instruction;
import net.runelite.deob.attributes.code.instruction.types.PushConstantInstruction;
import net.runelite.deob.execution.InstructionContext;

public class MultiplicationExpression
{
	List<InstructionContext> instructions = new ArrayList<>(); // push constant instructions that are being multiplied
	List<MultiplicationExpression> subexpressions = new ArrayList<>(); // for distributing, each subexpr is * by this
	static boolean replace;
	
	int simplify(int start)
	{
		int count = 0;
		int result = start;

		// calculate result
		for (InstructionContext i : instructions)
		{
			PushConstantInstruction pci = (PushConstantInstruction) i.getInstruction();
			int value = (int) pci.getConstant().getObject();

			result *= value;
		}
		
		// multiply subexpressions by result
		if (!subexpressions.isEmpty())
		{
			for (MultiplicationExpression me : subexpressions)
			{
				count += me.simplify(result);
			}
			
			result = 1; // constant has been distributed, outer numbers all go to 1
		}
		
		// set result on ins
		for (InstructionContext i : instructions)
		{
			PushConstantInstruction pci = (PushConstantInstruction) i.getInstruction();
			Instruction newIns = pci.setConstant(new net.runelite.deob.pool.Integer(result));
			++count;
			if (newIns != pci)
			{
				newIns.getInstructions().replace((Instruction) pci, newIns);
				replace = true;
			}
			result = 1; // rest of the results go to 1
		}
		
		return count;
	}
}
