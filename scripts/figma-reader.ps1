param(
    [string]$NodeId = "2637:753",
    [string]$FileKey = "x3gpCYiTabM6QSM1vMDhFl",
    [string]$Token = "figd_8S1V9v3Y7Qhlj8gh6QmZjI5cm7taoeqCRUEDtDNV"
)

$headers = @{ "X-Figma-Token" = $Token }

function Convert-ToHex($color) {
    if ($null -eq $color) { return "N/A" }
    try {
        $r = [math]::Round([double]$color.r * 255)
        $g = [math]::Round([double]$color.g * 255)
        $b = [math]::Round([double]$color.b * 255)
        return ("#{0:X2}{1:X2}{2:X2}" -f $r, $g, $b)
    } catch {
        return "N/A"
    }
}

function Get-NodeInfo($node, $indent = "") {
    if ($null -eq $node) { return }
    
    $info = "$indent$($node.type): $($node.name)"
    
    if ($node.absoluteBoundingBox) {
        $w = $node.absoluteBoundingBox.width
        $h = $node.absoluteBoundingBox.height
        $info += " | ${w}x${h}"
    }
    
    if ($node.fills -and $node.fills.Count -gt 0) {
        $fill = $node.fills[0]
        if ($fill.color) {
            $info += " | COLOR: $(Convert-ToHex $fill.color)"
        }
    }
    
    if ($node.style) {
        $s = $node.style
        if ($s.fontSize) { $info += " | FONT: $($s.fontSize)sp" }
        if ($s.fontWeight) { $info += " w=$($s.fontWeight)" }
        if ($s.fontFamily) { $info += " [$($s.fontFamily)]" }
    }
    
    if ($node.characters) {
        $txt = $node.characters
        if ($txt.Length -gt 60) { $txt = $txt.Substring(0, 60) + "..." }
        $info += " | `"$txt`""
    }
    
    if ($node.cornerRadius) { $info += " | R=$($node.cornerRadius)" }
    if ($node.rectangleCornerRadii) { $info += " | R=$($node.rectangleCornerRadii -join ',')" }
    if ($node.paddingLeft -ne $null) { $info += " | PAD: L=$($node.paddingLeft) T=$($node.paddingTop) R=$($node.paddingRight) B=$($node.paddingBottom)" }
    if ($null -ne $node.itemSpacing) { $info += " | GAP: $($node.itemSpacing)" }
    if ($node.layoutMode) { $info += " | FLEX: $($node.layoutMode)" }
    
    Write-Output $info
    
    if ($node.children) {
        foreach ($child in $node.children) {
            Get-NodeInfo $child "$indent  "
        }
    }
}

Write-Output "=== FIGMA DATA ==="
Write-Output "File: $FileKey | Node: $NodeId"
Write-Output ""

try {
    $response = Invoke-RestMethod -Uri "https://api.figma.com/v1/files/$FileKey/nodes?ids=$NodeId&depth=10" -Headers $headers -ErrorAction Stop
    $node = $response.nodes.$NodeId.document
    Get-NodeInfo $node
} catch {
    Write-Output "ERROR: $($_.Exception.Message)"
}
